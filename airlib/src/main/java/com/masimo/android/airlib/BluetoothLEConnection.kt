package com.masimo.android.airlib

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import com.masimo.common.logging.Log
import java.util.*

private val LOG = Log.tag("BLE_CONN")

private const val MAX_MTU = 512
const val RESULT_OK = 0
const val RESULT_ERROR_INVALID_STATE = -1
const val RESULT_ERROR_BT_UNAVAILABLE = -2
const val RESULT_ERROR_INVALID_DEVICE = -3
const val RESULT_ERROR_BT_DISABLED = -4

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
class BluetoothLEConnection(ctx: Context, listener: BLEConnectionListener, callback: AirProtocolDataCallback) :
        AirProtocolStateListener {
    private val LOCK = Any()
    private val handler = Handler()
    private val mBluetoothAdapter: BluetoothAdapter?
    private val dataCallback: AirProtocolDataCallback = callback

    var deviceAddress: String? = null
        private set
    var userDisconnect = false
        private set

    private var mBleCharacteristicConfig: BLECharacteristicConfig? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mOutgoingCharacteristic: BluetoothGattCharacteristic? = null
    private var protocol: AbstractProtocol? = null
    private var mListener: NewDataListener? = null

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val context: Context = ctx.applicationContext
    private var connectionListener: BLEConnectionListener? = listener

    @Volatile
    private var connectionState = ConnectionState.DISCONNECTED

    @Volatile
    private var activeScanCallback: ScanCallback? = null

    init {
        val btMgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = btMgr.adapter
    }

    private fun onGATTConnected(gatt: BluetoothGatt) {
        LOG.d("GATT connect")
        synchronized(LOCK) {
            if (gatt != mBluetoothGatt) {
                LOG.e("Invalid GATT connected. Disconnecting.")
                gatt.disconnect()
                mOutgoingCharacteristic = null
                return
            }
            setConnectionState(ConnectionState.DISCOVERY)
            LOG.d("Starting services discovery.")
            mBluetoothGatt!!.discoverServices()
        }
    }

    private fun canScan(): Boolean {
        synchronized(LOCK) { return connectionState == ConnectionState.DISCONNECTED }
    }

    @SuppressLint("MissingPermission") fun  // caller should have permission
            startScan(callback: ScanCallback?) {
        synchronized(LOCK) {
            if (!canScan()) {
                LOG.e("Not performing scan. Already doing something else.")
                return
            }
            val leScanner = mBluetoothAdapter!!.bluetoothLeScanner
            if (leScanner == null) {
                LOG.w("BluetoothLeScanner not found")
                return
            }
            activeScanCallback = callback
            leScanner.startScan(callback)
        }
    }

    @SuppressLint("MissingPermission") fun  // caller should have permission
            stopScan() {
        synchronized(LOCK) {
            if (activeScanCallback == null) {
                LOG.w("Not scanning.")
                return
            }
            val leScanner = mBluetoothAdapter!!.bluetoothLeScanner
            if (leScanner == null) {
                LOG.w("BluetoothLeScanner not found")
                return
            }
            leScanner.stopScan(activeScanCallback)
            activeScanCallback = null
        }
    }

    private fun onGATTDisconnected(gatt: BluetoothGatt) {
        LOG.d("GATT disconnect")
        if (gatt != mBluetoothGatt) {
            LOG.e("Invalid GATT disconnected.")
            gatt.close()
        }
        disconnect(false)
    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @Synchronized override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED    -> onGATTConnected(gatt)
                BluetoothProfile.STATE_DISCONNECTED -> onGATTDisconnected(gatt)
                else                                -> LOG.d("GATT status: $status, state: $newState")
            }
        }

        @Synchronized override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            mOutgoingCharacteristic = mBluetoothGatt!!.getService(mBleCharacteristicConfig!!.serviceCharacteristic)
                .getCharacteristic(mBleCharacteristicConfig!!.outgoingCharacteristic)
            if (mOutgoingCharacteristic == null || status != BluetoothGatt.GATT_SUCCESS) {
                LOG.w("onDescriptorWrite failed")
                disconnect(false)
                return
            }
            mBluetoothGatt!!.requestMtu(MAX_MTU)
        }

        @Synchronized override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                LOG.w("onMtuChanged failed")
                return
            }

            LOG.d("MTU size changed. New size = $mtu")
            setConnectionState(ConnectionState.CONNECTED)
            start(protocol)
            if (mListener == null) return
            mListener!!.onDescriptorWrite()
        }

        @Synchronized override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            if (mListener == null) return
            mListener!!.onDescriptorWrite()
        }

        @Synchronized override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val characteristic = gatt.getService(mBleCharacteristicConfig!!.serviceCharacteristic)
                    .getCharacteristic(mBleCharacteristicConfig!!.incomingCharacteristic)
                mBluetoothGatt!!.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(mBleCharacteristicConfig!!.notificationDescriptorCharacteristic)
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                //Add delay to avoid status 8 disconnect when connecting
                handler.postDelayed({ if (mBluetoothGatt != null) mBluetoothGatt!!.writeDescriptor(descriptor) }, 500)
                LOG.d("Services discovered. Connected.")
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                disconnect(false)
                LOG.w("onServicesDiscovered received: Status =  GATT_INSUFFICIENT_AUTHENTICATION")
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
                disconnect(false)
                LOG.w("onServicesDiscovered received: Status =  GATT_INSUFFICIENT_ENCRYPTION")
            } else {
                disconnect(false)
                LOG.w("onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            // do nothing
        }

        @Synchronized override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (mListener == null || mBleCharacteristicConfig!!.incomingCharacteristic != characteristic.uuid) return
            mListener!!.onNewData(characteristic.value)
        }
    }

    private fun registerBleDeviceReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        intentFilter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        context.registerReceiver(mBluetoothDeviceReceiver, intentFilter)
    }

    /**
     * Unregisters Bluetooth State Receiver. Should be called when the device Scanner is no longer needed.
     */
    private fun unRegisterBleReceiver(receiver: BroadcastReceiver) {
        try {
            context.unregisterReceiver(receiver)
        } catch (ex: Exception) {
            //            LOG.d("Receiver is already unregistered");
        }
    }

    /**
     * Receiver for Listening to the Bluetooth State on the device. When State changes an event is
     * dispatched notifying observers of the ReadyState.
     */
    private val mBluetoothDeviceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @Synchronized override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                LOG.d("Ble BondingState changed to $state")
                when (state) {
                    BluetoothDevice.BOND_BONDING -> {
                    }
                    BluetoothDevice.BOND_BONDED  -> {
                        val device = mBluetoothAdapter!!.getRemoteDevice(deviceAddress)
                        startGattServiceConnection(device)
                        unRegisterBleReceiver(this)
                    }
                    BluetoothDevice.BOND_NONE    ->                     //Unable to Bond send disconnect message
                        setConnectionState(ConnectionState.DISCONNECTED)
                }
            } else if (action == BluetoothDevice.ACTION_PAIRING_REQUEST) {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR)
                LOG.d("BLE ACTION_PAIRING_REQUEST changed to $state")
                when (state) {
                    BluetoothDevice.PAIRING_VARIANT_PIN -> {
                        LOG.d("BluetoothDeviceLE ACTION_PAIRING_REQUEST State changed to PAIRING_VARIANT_PIN")
                        updatePin()
                        abortBroadcast()
                    }
                    BluetoothDevice.ERROR               -> LOG.d("ACTION_PAIRING_REQUEST Error occurred ")
                }
            }
        }
    }

    @Synchronized private fun updatePin() {
        val device = mBluetoothAdapter!!.getRemoteDevice(deviceAddress)
        if (device != null) {
            val pin = PinGenerator.GeneratePinFromMac(deviceAddress)
            LOG.d(String.format("BLE Pin as byte[] = %s", Arrays.toString(pin)))
            val isPinSet = device.setPin(pin)
            LOG.d("BLEDevice isPinSet = $isPinSet")
        }
    }

    override fun onMaxRetries() {
        LOG.e("Max retries reached. Restarting protocol.")
        stop(protocol)
        protocol = null
        createProtocol()
        start(protocol)
    }

    @Synchronized private fun createProtocol() {
        if (protocol != null) {
            LOG.e("Already connected. Not creating protocol.")
            return
        }
        protocol = AirParser(context, dataCallback, this)
        connectionListener?.onProtocolCreated()
        // DO NOT START PROTOCOL YET
    }

    @Synchronized fun write(data: ByteArray, length: Int): Boolean {
        if (mOutgoingCharacteristic == null || mBluetoothGatt == null) {
            LOG.d("Unable to write. No outgoing characteristic. ")
            return false
        }

        mOutgoingCharacteristic!!.value = data
        LOG.d(String.format("Setting Outgoing Data with length %d", data.size))

        return mBluetoothGatt!!.writeCharacteristic(mOutgoingCharacteristic)
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param btAddress The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    @SuppressLint("MissingPermission") // caller should have this permission
    @Synchronized fun connect(btAddress: String?): Int {
        if (connectionState == ConnectionState.CONNECTING) {
            LOG.d("Already connecting... patience, please.")
        }

        userDisconnect = false

        if (connectionState != ConnectionState.DISCONNECTED && connectionState != ConnectionState.CONNECTING) {
            LOG.e("Current connection state does not allow for new connections: $connectionState")
            return RESULT_ERROR_INVALID_STATE
        }

        synchronized(LOCK) {
            stopScan()
            if (mBluetoothAdapter == null) {
                LOG.e("BluetoothAdapter not available.")
                return RESULT_ERROR_BT_UNAVAILABLE
            }
            if (!mBluetoothAdapter.isEnabled) {
                LOG.e("Bluetooth is not enabled")
                return RESULT_ERROR_BT_DISABLED
            }
            deviceAddress = btAddress
            if (TextUtils.isEmpty(deviceAddress)) {
                LOG.e("Invalid device address: $deviceAddress")
                return RESULT_ERROR_INVALID_DEVICE
            }
            setConnectionState(ConnectionState.CONNECTING)
            val device = mBluetoothAdapter.getRemoteDevice(deviceAddress)
            createProtocol()
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                LOG.d("Device is already bonded")
                startGattServiceConnection(device)
            } else {
                LOG.d("Trying to establish a bond")
                registerBleDeviceReceiver()
                device.createBond()
            }
            return RESULT_OK
        }
    }

    @Synchronized private fun startGattServiceConnection(device: BluetoothDevice?) {
        if (device == null) {
            LOG.w("Device not found.  Unable to connect.")
            return
        }
        if (mBluetoothGatt != null) {
            LOG.w("Connection request already in progress")
            return
        }
        LOG.d("Starting a new Gatt Service Connection")
        mBleCharacteristicConfig = createAIRConfig()
        mBluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, mGattCallback)
        }
        LOG.d("Trying to create a new connection.")
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    @Synchronized fun disconnect(isUserDisconnect: Boolean) {
        userDisconnect = isUserDisconnect
        if (connectionState != ConnectionState.CONNECTED && connectionState != ConnectionState.CONNECTING && connectionState != ConnectionState.DISCOVERY) {
            LOG.e("Cannot disconnect in current state: $connectionState")
            return
        }
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LOG.w("BluetoothAdapter not initialized")
            return
        }
        setConnectionState(ConnectionState.DISCONNECTING)
        mOutgoingCharacteristic = null
        mBluetoothGatt!!.disconnect()
        close()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (null != mBluetoothGatt) {
            LOG.d("Closing bluetooth gatt.")
            mBluetoothGatt!!.close()
            mBluetoothGatt = null
        }
        stop(protocol)
        protocol = null
        setConnectionState(ConnectionState.DISCONNECTED)
    }

    fun setConnectionListener(listener: BLEConnectionListener?) {
        this.connectionListener = listener
    }

    private fun setConnectionState(state: ConnectionState) {
        LOG.d("Setting ConnectionState : $state")
        connectionState = state
        connectionListener?.onConnectionStateChanged(connectionState)
    }

    @Synchronized fun setNewDataListener(newDataListener: NewDataListener?) {
        mListener = newDataListener
    }

    fun start(protocol: AbstractProtocol?) {
        if (protocol == null) {
            LOG.e("No protocol")
            disconnect(false)
            return
        }
        LOG.d("Starting Protocol " + protocol.javaClass.simpleName)
        protocol.connection = this
        protocol.initialize()
    }

    fun stop(protocol: AbstractProtocol?) {
        if (protocol == null) return
        LOG.d("Stopping Protocol " + protocol.javaClass.simpleName)
        protocol.deinitialize()
    }

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, DISCOVERY, CONNECTED, DISCONNECTING
    }

    interface BLEConnectionListener {
        fun onConnectionStateChanged(state: ConnectionState)
        fun onProtocolCreated()
    }

    interface NewDataListener {
        fun onDescriptorWrite()
        fun onNewData(bytes: ByteArray)
    }

    val isBLEConnected: Boolean
        get() = connectionState == ConnectionState.CONNECTED

    val isDisconnectedManually: Boolean
        get() = !isBLEConnected && userDisconnect
}