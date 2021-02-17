package com.masimo.android.airlib

import android.content.Context
import com.masimo.android.airlib.BluetoothLEConnection.NewDataListener
import com.masimo.communication.base.model.data.ProtocolPacket

abstract class AbstractProtocol(protected val context: Context) : NewDataListener {
    internal var connection: BluetoothLEConnection? = null
    set(value) {
        onRemoveConnection(field)
        field = value
        onNewConnection(field)
    }

    protected open fun onNewConnection(mConnection: BluetoothLEConnection?) {
        mConnection?.setNewDataListener(this)
    }

    protected open fun onRemoveConnection(mConnection: BluetoothLEConnection?) {
        mConnection?.setNewDataListener(null)
    }

    internal  var mIsSensorOnFinger = false
    internal  var waveformCallback: WaveformCallback? = null

    @Synchronized open fun deinitialize() {
        mIsSensorOnFinger = false
        waveformCallback?.reset()
        connection = null
    }

    protected fun dispatchCommand(protocolPacket: ProtocolPacket) = protocolPacket.data.let {
        connection?.write(it, it.size)
        return@let
    }

    abstract fun initialize(): Boolean
    interface WaveformCallback {
        fun reset()
        fun newValue(timestamp: Long, pleth: Float, siq: Float)
    }
}