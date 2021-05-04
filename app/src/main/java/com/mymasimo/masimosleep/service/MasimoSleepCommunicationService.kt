package com.mymasimo.masimosleep.service

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.masimo.android.airlib.*
import com.masimo.common.model.universal.ParameterID
import com.masimo.sleepscore.sleepscorelib.model.Parameter
import com.mymasimo.masimosleep.BuildConfig.APPLICATION_ID
import com.mymasimo.masimosleep.MainActivity
import com.mymasimo.masimosleep.MasimoSleepApp
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SensorRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.room.entity.Module
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.model.Tick
import com.mymasimo.masimosleep.util.CHANNEL_SYSTEM
import com.mymasimo.masimosleep.util.ExceptionMaskUtil
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val SVC_LOCK = Any()

private const val ACTION_CONNECT_BLE = "$APPLICATION_ID.action.CONNECT_BLE"
private const val ACTION_DISCONNECT_BLE = "$APPLICATION_ID.action.DISCONNECT_BLE"
private const val ACTION_START_LOCATION_UPDATES = "$APPLICATION_ID.action.START_LOACTION_UPDATES"
private const val ACTION_STOP_LOCATION_UPDATES = "$APPLICATION_ID.action.STOP_LOCATION_UPDATES"
private const val ACTION_USER_DECLINE_BT = "$APPLICATION_ID.action.USER_DECLINED_BT"
private const val ACTION_START_BLE_SCAN = "$APPLICATION_ID.action.START_BLE_SCAN"
private const val ACTION_STOP_BLE_SCAN = "$APPLICATION_ID.action.STOP_BLE_SCAN"

const val ACTION_BT_STATUS = "$APPLICATION_ID.action.BT_STATUS"
const val ACTION_BLE_SCAN_TIMEOUT = "$APPLICATION_ID.action.BLE_SCAN_TIMEOUT"

const val EXTRA_BT_ENABLED = "bt_enabled"
const val EXTRA_MODULE_ID = "module_id"

private const val BLE_SCAN_DURATION_MINS = 2L   // minutes

private const val PERMISSION_APP_PRIVATE = "${APPLICATION_ID}.permission.APP_PRIVATE"

val activeServiceModuleIDUpdates = MutableLiveData<Long>().apply { value = 0L }

fun isDeviceConnected(): Boolean = activeServiceModuleIDUpdates.value != 0L

private var serviceInstanceId: Int = 0
    set(value) {
        field = value % Int.MAX_VALUE
    }

private fun serviceIntent(context: Context, action: String? = null): Intent {
    return Intent(context, MasimoSleepCommunicationService::class.java).also { intent ->
        action?.let { intent.action = it }
    }
}

fun serviceConnectBLE(context: Context) {
    Timber.d("Starting service to connect to BLE device")
    startForegroundService(context, serviceIntent(context, ACTION_CONNECT_BLE))
}

fun serviceDisconnectBLE(context: Context, id: Long = 0L) {
    Timber.d("Disconnect BLE service")
    context.startService(serviceIntent(context, ACTION_DISCONNECT_BLE).apply {
        if (id > 0) putExtra(EXTRA_MODULE_ID, id)
    })
}

fun startBLEScan(context: Context) {
    startForegroundService(context, serviceIntent(context, ACTION_START_BLE_SCAN))
}

fun stopBLEScan(context: Context) {
    startForegroundService(context, serviceIntent(context, ACTION_STOP_BLE_SCAN))
}

class MasimoSleepCommunicationService : Service(), BluetoothLEConnection.BLEConnectionListener, CoroutineScope by CoroutineScope(Dispatchers.IO) {

    @Inject
    lateinit var sleepSessionScoreManager: SleepSessionScoreManager
    @Inject
    lateinit var bleConnectionState: BLEConnectionState
    @Inject
    lateinit var deviceExceptionHandler: DeviceExceptionHandler
    @Inject
    lateinit var disposables: CompositeDisposable
    @Inject
    lateinit var sessionRepository: SessionRepository
    @Inject
    lateinit var sensorRepository: SensorRepository
    @Inject
    lateinit var programRepository: ProgramRepository
    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val LOCK = Any()

    private lateinit var handlerThread: HandlerThread
    private lateinit var scheduler: Scheduler
    private lateinit var scanHandler: BLEScanHandler
    private var bleStateReceiver: BroadcastReceiver? = null

    private lateinit var bleConnection: BluetoothLEConnection

    private var currentModule: Module? = null
        set(value) {
            field = value
            tryConnectBLE()
        }

    private var reconnectTimer: Disposable? = null
    private var bleScanTimeout: Disposable? = null

    private var fgNotification: NotificationCompat.Builder? = null

    private val notificationMgr: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val ppgDeviceName: String by lazy { getString(R.string.radius_ppg) }
    private val appName: String by lazy { getString(R.string.app_name) }

    private val instanceId = serviceInstanceId++

    private val fgObserver = Observer<Boolean> { isInForeground ->
        val inBackground = !isInForeground
        val sessionInProgress = sleepSessionScoreManager.isSessionInProgress
        Timber.d("Is app in background=$inBackground sessionInProgress=$sessionInProgress")
        if (inBackground && !sessionInProgress) {
            // No need to keep the service running if there's no session in progress..

            if (!bleConnection.isBLEConnected) {
                Timber.d("Not connected, stopping service directly")
                bleConnection.setConnectionListener(null)
                stopService()
            } else {
                Timber.d("Disconnecting BLE, then stopping service")
                disconnectBLE(id = currentModule?.id ?: -1, user = true)
            }
        }
    }

    override fun onCreate() {
        Injector.get().inject(this)
        super.onCreate()

        handlerThread = HandlerThread("MasimoSleepService").apply { start() }
        scanHandler = BLEScanHandler(
            handlerThread.looper,
            LocalBroadcastManager.getInstance(applicationContext)
        )
        scheduler = AndroidSchedulers.from(handlerThread.looper)

        bleConnection = BluetoothLEConnection(this, this, dataCallback)

        fgNotification = NotificationCompat.Builder(this, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(appName)
            .setDefaults(NotificationCompat.DEFAULT_ALL xor NotificationCompat.DEFAULT_SOUND xor NotificationCompat.DEFAULT_VIBRATE)
            .setSound(null)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .also {
                startForeground(R.id.notification_service_fg, it.build())
            }
        updateNotification()

        // register service
        registerBLEStateReceiver()

        launch { sensorRepository.loadCurrentSensor().collect { currentModule = it } }
        MasimoSleepApp.get().foreground.observeForever(fgObserver)

        observeSensorOffExceptions()

        Timber.d("Service created with instance id $instanceId")
    }

    override fun onDestroy() {
        Timber.d("Destroying BLE service")
        MasimoSleepApp.get().foreground.removeObserver(fgObserver)

        disconnectBLE()
        notificationMgr.cancel(R.id.notification_service_fg)

        bleStateReceiver?.let {
            unregisterReceiver(it)
        }

        stopConstantReconnectBLETask()

        disposables.dispose()
        coroutineContext.cancel()

        if (!handlerThread.quitSafely()) {
            Completable.timer(1L, TimeUnit.SECONDS)
                .subscribeOn(scheduler)
                .observeOn(Schedulers.trampoline())
                .subscribe(HandlerThreadDelayedTerminator())
        } else Timber.d("Handler thread shut down safely.")

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.action?.let { action ->
            Timber.d("Servicing action: $action")
            when (action) {
                ACTION_CONNECT_BLE -> tryConnectBLE()
                ACTION_DISCONNECT_BLE -> disconnectBLE(id = intent.getLongExtra(EXTRA_MODULE_ID, 0L))
                ACTION_START_LOCATION_UPDATES -> TODO()
                ACTION_STOP_LOCATION_UPDATES -> TODO()
                ACTION_USER_DECLINE_BT -> showBtDisabledNotification()
                ACTION_START_BLE_SCAN -> startScan()
                ACTION_STOP_BLE_SCAN -> stopScan()
                else -> Timber.d("Ignoring unknown action $action")
            }
        } ?: tryConnectBLE()
        return START_STICKY
    }

    private fun startScan() = synchronized(LOCK) {
        scanHandler.reset()
        bleConnection.startScan(bleScanCallback)
        bleConnectionState.setCurrentState(State.SEARCHING)
        bleScanTimeout = Observable.timer(BLE_SCAN_DURATION_MINS, TimeUnit.MINUTES)
            .subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                onBLEScanTimeout()
            }
            .subscribe()
    }

    private fun stopScan() {
        bleConnection.stopScan()
        bleConnectionState.setCurrentState(State.NO_DEVICE_CONNECTED)
        bleScanTimeout?.let { disposable ->
            disposable.dispose()
            bleScanTimeout = null
        }
    }

    private fun onBLEScanTimeout() {
        Timber.d("Scan timed out")
        scanHandler.timedOut()
        stopScan()
    }

    private fun startConstantReconnectBLETask() {
        reconnectTimer?.dispose()

        if (currentModule == null) return

        Timber.d("Will attempt to reconnect every 200 milliseconds...")
        reconnectTimer = Observable.interval(0, 200, TimeUnit.MILLISECONDS, scheduler)
            .doOnSubscribe {
                bleConnectionState.setCurrentState(State.SEARCHING)
            }.subscribe {
                Timber.d("Attempting to reconnect...")
                tryConnectBLE()
            }
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    private fun stopConstantReconnectBLETask() = synchronized(LOCK) {
        reconnectTimer?.let { disposable ->
            Timber.d("Stopping constant reconnect attempts")
            disposable.dispose()
            reconnectTimer = null
        }
    }

    private fun connectBLE() = synchronized(LOCK) {
        if (bleConnection.isBLEConnected) {
            bleConnectionState.setCurrentState(State.DEVICE_CONNECTED)
            Timber.d("Already connected")
            stopConstantReconnectBLETask()
            return@synchronized
        }

        currentModule?.let {
            Timber.d("Loading Air Communication for $it")

            val shouldStartReconnectTask = when (val connect = bleConnection.connect(it.address)) {
                RESULT_OK -> {
                    Timber.d("BLE connection starting")
                    false
                }
                RESULT_ERROR_INVALID_DEVICE -> {
                    // TODO: What do we do regarding an invalid device record?
                    false
                }
                RESULT_ERROR_BT_UNAVAILABLE -> {
                    // TODO:
                    false
                }
                RESULT_ERROR_BT_DISABLED -> {
                    // TODO: let the user know to enable BT
                    true
                }
                else -> {
                    Timber.w("Received code $connect from connect.")
                    true
                }
            }

            if (shouldStartReconnectTask) {
                startConstantReconnectBLETask()
            }
        } ?: run {
            Timber.d("connectBLE(): No module paired")
            null
        }
    }

    private fun tryConnectBLE() = synchronized(LOCK) {
        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled != true) {
            Timber.e("Bluetooth not enabled")
            bleConnectionState.setCurrentState(State.BLE_DISCONNECTED)
            return@synchronized
        }

        val module = currentModule
        if (module == null) {
            Timber.e("No device to connect to.")
            bleConnectionState.setCurrentState(State.NO_DEVICE_CONNECTED)
            stopConstantReconnectBLETask()
        } else {
            Timber.d("Connecting to BLE device $module")
            if (MasimoSleepPreferences.emulatorUsed) {
                Timber.d("work with emulator")
                bleConnectionState.setCurrentState(State.DEVICE_CONNECTED)
                stopConstantReconnectBLETask()
                launch { sensorRepository.getTicks(module).collect { tick ->
                    Timber.d("Tick from emulator: $tick")
                    sleepSessionScoreManager.sendTick(tick)
                } }
            } else connectBLE()
        }
    }

    private fun disconnectBLE(id: Long = 0L, user: Boolean = false) = synchronized(LOCK) {
        if (bleConnection.isDisconnectedManually) {
            Timber.d("Not currently connected.")
            return@synchronized
        }

        launch {
            sensorRepository.getCurrentSensor()?.let { sensor ->
                if (id != 0L && id != sensor.id) {
                    Timber.d("current module id = ${sensor.id}. Disconnect requested for id $id. Not disconnecting.")
                    return@launch
                }

                Timber.i("Disconnecting from $sensor")
                bleConnection.disconnect(user)
                bleConnectionState.setCurrentState(State.NO_DEVICE_CONNECTED)
            } ?: run {
                Timber.e("No current module, but somehow connected")
            }
        }
    }

    private fun updateNotification() = synchronized(SVC_LOCK) {
        fgNotification?.let { builder ->
            val isConnected = bleConnection.isBLEConnected

            Timber.d("Service SHOULD be in foreground")

            val notification = builder
                .setWhen(System.currentTimeMillis())

            val msg = if (isConnected) getString(R.string.monitoring_via_x, ppgDeviceName)
            else getString(R.string.searching_for_x, ppgDeviceName)

            notification.setContentText(msg)

            Timber.d("Updating foreground notification")
            notificationMgr.notify(R.id.notification_service_fg, builder.setWhen(System.currentTimeMillis()).build())
        } ?: run {
            Timber.e("No notification to update")
        }
    }

    private fun registerBLEStateReceiver() {
        bleStateReceiver = BLEStateReceiver()
        registerReceiver(bleStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    private inner class BLEStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("GATT: ${intent.action}")

            if (BluetoothAdapter.ACTION_STATE_CHANGED != intent.action) {
                Timber.e("Unrecognized BLE action: ${intent.action}")
                return
            }

            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_OFF -> {
                    Timber.e("BT turned off")
                    onBTStateChanged(false)
                }
                BluetoothAdapter.STATE_ON -> {
                    Timber.e("BT turned on")
                    onBTStateChanged(true)
                }
            }
        }
    }

    private fun onBTStateChanged(isEnabled: Boolean) = synchronized(LOCK) {
        if (isEnabled) {
            bleConnectionState.setCurrentState(State.NO_DEVICE_CONNECTED)
            if (!bleConnection.isBLEConnected) tryConnectBLE()
            // Remove BT enable notification
            notificationMgr.cancel(R.id.notification_service_bt)
        } else {
            bleConnectionState.setCurrentState(State.BLE_DISCONNECTED)
            disconnectBLE()
        }

        // Send an ordered broadcast
        val btIntent = Intent(ACTION_BT_STATUS).apply {
            putExtra(EXTRA_BT_ENABLED, isEnabled)
        }

        sendOrderedBroadcast(btIntent, PERMISSION_APP_PRIVATE, BluetoothChangeResultReceiver(), null, Activity.RESULT_CANCELED, null, null)
    }

    inner class BluetoothChangeResultReceiver : BroadcastReceiver() {
        /**
         * Captures the final result after all the BroadcastReceivers are executed.
         */
        override fun onReceive(context: Context, intent: Intent) {
            if (resultCode == Activity.RESULT_OK) {
                // Remove BT enable notification if handled by UI
                notificationMgr.cancel(R.id.notification_service_bt)
                return
            }

            // If bt not enabled yet or handled by UI
            if (intent.hasExtra(EXTRA_BT_ENABLED) && !intent.getBooleanExtra(EXTRA_BT_ENABLED, false)) {
                showBtDisabledNotification()
            }
        }
    }

    private fun showBtDisabledNotification() {
        // Display notification for disabling bluetooth
        val message = getString(R.string.enable_bt_for_monitoring)
        val btNotification = NotificationCompat.Builder(this, CHANNEL_SYSTEM)
            .setContentTitle(getString(R.string.bt_disabled))
            .setSmallIcon(R.drawable.ic_stat_bt_disabled)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .addAction(
                R.drawable.ic_bluetooth,
                getString(R.string.enable),
                PendingIntent.getActivity(
                    this,
                    1,
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setContentText(message)
            .setTicker(message)
            .setOngoing(true)
            .setWhen(System.currentTimeMillis())
            .build()

        notificationMgr.notify(R.id.notification_service_bt, btNotification)
    }

    // no bind
    override fun onBind(intent: Intent?): IBinder? = null

    private fun onBLEDisconnected() {
        val userDisconnect = bleConnection.userDisconnect
        Timber.d("BLE disconnected ${if (userDisconnect) "by user" else "remotely"}")

        activeServiceModuleIDUpdates.postValue(0)

        sleepSessionScoreManager.startDisconnectionEndSessionCountDown()

        bleConnectionState.setCurrentState(State.NO_DEVICE_CONNECTED)
        sleepSessionScoreManager.interrupt()

        //Only clear SENSOR_OFF_PATIENT, DEFECTIVE_SENSOR, LOW_BATTERY when disconnected because we can receive empty exception set in between exception sets.
        deviceExceptionHandler.clearExceptions()

        if (userDisconnect) stopConstantReconnectBLETask()
        else startConstantReconnectBLETask()
    }

    private fun onBLEConnected() = synchronized(LOCK) {
        val module = currentModule
            ?: throw IllegalStateException("Current module can't be null if we're connected")
        Timber.d("Connected to $module")
        bleConnectionState.setCurrentState(State.DEVICE_CONNECTED)
        stopConstantReconnectBLETask()
        activeServiceModuleIDUpdates.postValue(module.id)
        sleepSessionScoreManager.stopDisconnectionEndSessionCountDown()
    }

    private inner class HandlerThreadDelayedTerminator : CompletableObserver {
        override fun onComplete() {
            handlerThread.quit()
            Timber.d("Handler thread shut down.")
        }

        override fun onSubscribe(d: Disposable) {
            Timber.w("Handler thread did not qo quietly. I'll be back!")
        }

        override fun onError(e: Throwable) {
            Timber.e(RuntimeException("Service handler thread did not quit", e))
        }
    }

    override fun onConnectionStateChanged(state: BluetoothLEConnection.ConnectionState) = synchronized(SVC_LOCK) {
        Timber.d("onConnectionStateChanged state: $state")
        when (state) {
            BluetoothLEConnection.ConnectionState.CONNECTED -> onBLEConnected()
            BluetoothLEConnection.ConnectionState.DISCONNECTED -> onBLEDisconnected()
            else -> Timber.d("BLE connection state: $state")
        }
        updateNotification()
    }

    override fun onProtocolCreated() {
        // nothing
    }

    private val bleScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            scanHandler.post {
                scanHandler.onMatchFound(result, callbackType)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Timber.e("BLE scan failed: $errorCode")
            scanHandler.scanFailed(errorCode)
        }
    }

    private val dataCallback = object : AirProtocolDataCallback {
        override fun onParameterData(params: AirProtocolParameterData) {
            Timber.d("Received new data from device: $params")

            deviceExceptionHandler.updateExceptions(params.exceptions)

            // Clear the sensor off flag only if BLE disconnected or received valid parameter data.
            if (params.spo2 != 0f || params.pr != 0f || params.rrp != 0f) {
                deviceExceptionHandler.clearSensorOffException()
            }

            val pSPO2 = Parameter(
                ParameterID.FUNC_SPO2,
                params.spo2,
                ExceptionMaskUtil.convertExceptionToMask(params.spo2Exceptions)
            )
            val pPR = Parameter(
                ParameterID.PR,
                params.pr,
                ExceptionMaskUtil.convertExceptionToMask(params.prExceptions)
            )
            val pRRP = Parameter(
                ParameterID.RRP,
                params.rrp,
                ExceptionMaskUtil.convertExceptionToMask(params.rrpExceptions)
            )
            sleepSessionScoreManager.sendTick(Tick(pSPO2, pPR, pRRP))
        }

        override fun onWaveformData(waveforms: AirProtocolWaveformData) {
            Timber.d("Waveforms: $waveforms")
        }
    }

    private fun observeSensorOffExceptions() {
        deviceExceptionHandler.exceptionUpdates
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { deviceExceptions ->
                if (deviceExceptions.contains(DeviceException.SENSOR_OFF_PATIENT) && sleepSessionScoreManager.isSessionInProgress) {
                    sleepSessionScoreManager.startSensorOffEndSessionCountDown()
                } else {
                    sleepSessionScoreManager.stopSensorOffEndSessionCountDown()
                }
            }
            .addTo(disposables)
    }

}
