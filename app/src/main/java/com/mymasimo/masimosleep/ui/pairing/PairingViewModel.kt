package com.mymasimo.masimosleep.ui.pairing

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.jakewharton.rxrelay2.PublishRelay
import com.masimo.android.airlib.MASIMO_BLUETOOTH_SIG_MANUFACTURER_ID
import com.masimo.android.airlib.ProductType
import com.masimo.android.airlib.ProductVariant
import com.masimo.android.airlib.ScanRecordParser
import com.masimo.common.model.universal.ParameterID
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.repository.SensorRepository
import com.mymasimo.masimosleep.data.room.entity.Module
import com.mymasimo.masimosleep.service.*
import com.mymasimo.masimosleep.util.DEFAULT_MANUFACTURER_NAME
import com.mymasimo.masimosleep.util.test.FakeTicker
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject

class PairingViewModel @Inject constructor(
    app: Application,
    private val fakeTicker: FakeTicker,
    private val sensorRepository: SensorRepository,
    private val disposables: CompositeDisposable,
) : AndroidViewModel(app) {

    private val _isScanning = MutableLiveData(false)

    private val _nearbyBleModules = MutableLiveData<List<Module>>(emptyList())
    val nearbyBleModules: LiveData<List<Module>>
        get() = _nearbyBleModules

    private val scanTimedOutRelay = PublishRelay.create<Unit>()
    val scanTimedOut: Observable<Unit>
        get() = scanTimedOutRelay

    private val goToSelectDeviceScreenRelay = PublishRelay.create<Unit>()
    val goToSelectDeviceScreen: Observable<Unit>
        get() = goToSelectDeviceScreenRelay

    private val _pairingFinish = MutableLiveData<Boolean>()
    val pairingFinish: LiveData<Boolean>
        get() = _pairingFinish

    val isBTEnabled: Boolean
        get() = BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false

    private fun isScanning() = _isScanning.value == true
    private val scannedDeviceCache = mutableMapOf<String, Module>()
    private val rssiMap = mutableMapOf<String, Int>()
    private val localBroadcastManager = LocalBroadcastManager.getInstance(app)

    fun saveBleModule(address: String) {
        val module = scannedDeviceCache[address] ?: return
        val m = SN_VALIDATOR.matcher(module.serialNumber)

        if (!m.matches()) {
            Timber.e("Invalid module serial number: ${module.serialNumber}")
            return
        }

        viewModelScope.launch {
            stopScanningDevices()
            sensorRepository.addSensor(module)
            onPairingFinish()
        }
    }

    fun startAutomaticConnectCountDown() {
        Observable.timer(2, TimeUnit.SECONDS)
            .doOnNext {
                _nearbyBleModules.value?.let {
                    if (it.size == 1) {
                        saveBleModule(it[0].address)
                    }

                }
            }
            .subscribe()
            .addTo(disposables)
    }

    fun startScanningDevices() {
        if (isScanning()) {
            stopScanningDevices()
        }

        Timber.d("Scanning devices...")
        _isScanning.postValue(true)

        _nearbyBleModules.value = emptyList()
        scannedDeviceCache.clear()

        val intentFilter = IntentFilter(ACTION_BLE_SCAN_RESULT).also {
            it.addAction(ACTION_BLE_SCAN_TIMEOUT)
            it.addAction(ACTION_BLE_SCAN_FAILED)
        }

        localBroadcastManager.registerReceiver(bleScanResultReceiver, intentFilter)
        startBLEScan(getApplication())
    }

    fun stopScanningDevices() {
        Timber.d("Scanning stopped...")
        reset()
        stopBLEScan(getApplication())
    }

    fun connectToEmulator() = viewModelScope.launch {
        stopScanningDevices()
        val address = MasimoSleepPreferences.name ?: "default"
        val module = Module(
            type = ProductType.OTHER,
            variant = ProductVariant.OTHER,
            manufacturerName = "",
            firmwareVersion = "",
            serialNumber = "",
            address = address,
            supportedParameters = EnumSet.of(ParameterID.PR)
        )

        sensorRepository.addSensor(module, isEmulator = true)
//      fakeTicker.createNights(6)
        onPairingFinish()
    }

    fun onPairingFinishComplete() {
        _pairingFinish.value = false
    }

    private fun onFoundBLEDevice(result: ScanResult) {
        Timber.d("Found BLE device")
        val module = result.toSupportedModule() ?: kotlin.run {
            Timber.d("Scan result is not a supported module")
            return
        }

        Timber.d("Found supported module: $module")
        rssiMap[module.address] = result.rssi
        if (scannedDeviceCache.containsKey(module.address)) {
            Timber.d("Device already in cache")
            return
        }

        scannedDeviceCache[module.address] = module
        publishNearbyDevices()
        goToSelectDeviceScreenRelay.accept(Unit)
    }

    private fun publishNearbyDevices() {
        _nearbyBleModules.postValue(scannedDeviceCache.values.sortedBy {
            rssiMap[it.address]
        }.toList())
    }

    private fun onLostBLEDevice(result: ScanResult) {
        rssiMap.remove(result.device.address)
        scannedDeviceCache.remove(result.device.address) ?: return
        publishNearbyDevices()
    }

    private fun onScanTimeout() {
        Timber.d("Scan timed out")
        scanTimedOutRelay.accept(Unit)
        reset()
    }

    private fun reset() {
        localBroadcastManager.unregisterReceiver(bleScanResultReceiver)
        scannedDeviceCache.clear()
        _isScanning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopScanningDevices()
    }

    private val bleScanResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_BLE_SCAN_RESULT -> {
                    val result = intent.getParcelableExtra<ScanResult>(EXTRA_SCAN_RESULT)

                    if (intent.getBooleanExtra(EXTRA_MATCH_LOST, false)) {
                        if (isScanning()) {
                            onLostBLEDevice(result)
                        }
                    } else {
                        onFoundBLEDevice(result)
                    }
                }
                ACTION_BLE_SCAN_TIMEOUT -> onScanTimeout()
                ACTION_BLE_SCAN_FAILED -> {
                    val code = intent.getIntExtra(
                        EXTRA_ERROR_CODE,
                        ScanCallback.SCAN_FAILED_INTERNAL_ERROR
                    )
                    Timber.d("Scan failed with error code: $code")
                }
            }
        }
    }

    private fun ScanResult.toSupportedModule(): Module? {
        val record = scanRecord ?: return null
        record.manufacturerSpecificData ?: return null

        val id = MASIMO_BLUETOOTH_SIG_MANUFACTURER_ID

        val manufacturerData = record.getManufacturerSpecificData(id) ?: return null

        val parser = ScanRecordParser(
            manufacturerData,
            setOf(ProductType.AIR_SPO2),
            setOf(ProductVariant.AIR_SPO2_P05),
            null
        ).apply {
            parse()
        }

        if (!parser.foundSupportedDevice) return null

        return Module(
            parser.productType,
            parser.productVariant,
            DEFAULT_MANUFACTURER_NAME,
            parser.firmwareVersion,
            parser.serialNumber.toString(),
            device.address,
            EnumSet.of(ParameterID.FUNC_SPO2, ParameterID.PR, ParameterID.PI, ParameterID.PVI, ParameterID.RRP)
        )
    }

    private fun onPairingFinish() {
        _pairingFinish.value = true
    }

    companion object {
        private val SN_VALIDATOR = Pattern.compile(
            "^([A-F0-9]{2}:){5}[A-F0-9]{2}$",
            Pattern.CASE_INSENSITIVE
        )
    }
}