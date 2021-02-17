package com.masimo.android.airlib

import android.content.Context
import com.masimo.common.logging.Log
import com.masimo.common.model.request.BaseUIRequest
import com.masimo.common.model.universal.WaveformID
import com.masimo.communication.base.model.data.ProtocolPacket
import com.masimo.communication.base.model.data.WaveformData
import com.masimo.communication.base.model.event.WaveformDataEvent
import com.masimo.communication.base.protocol.IProtocol.ProtocolListener
import com.masimo.communication.utility.ProtocolUtil
import com.masimo.masimo.protocol.air.AirProtocol
import com.masimo.masimo.protocol.air.constants.AirConstants
import com.masimo.masimo.protocol.air.model.data.AirRequestType
import com.masimo.masimo.protocol.air.model.data.DataGroupType
import com.masimo.masimo.protocol.air.model.data.WaveformSelection
import com.masimo.masimo.protocol.air.model.data.group.ParameterDataGroup
import com.masimo.masimo.protocol.air.model.request.ConfigurePeriodicParameterGroupRequest
import com.masimo.masimo.protocol.air.model.request.ConfigurePeriodicWaveformRequest
import com.masimo.masimo.protocol.air.model.response.AckResponse
import com.masimo.masimo.protocol.air.model.response.DataGroupResponse
import com.masimo.masimo.protocol.air.model.response.NakResponse
import com.masimo.masimo.protocol.air.translator.AirTranslator
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

private const val PARAMETER_STREAMING_RATE_MS = 1000
private const val WAVEFORM_STREAMING_RATE_MS = 32
private const val MAX_START_STREAM_ATTEMPTS = 3
private const val PARAM_STREAM_TIMEOUT_SECONDS = 1

internal class AirParser(context: Context, private val callback: AirProtocolDataCallback, private val stateListener: AirProtocolStateListener) : AbstractProtocol(context),
                                                                                                                                        ProtocolListener {
    private val LOG = Log.tag("AIR_PARSER")

    private val airProtocol = AirProtocol()
    private val mTranslator = AirTranslator()
    private var startStreamTimeout: Disposable? = null
    private var mWaveformQueue: WaveformQueue? = WaveformQueue()
    private var mWaveformTimer: Timer? = Timer()

    override fun onNewConnection(mConnection: BluetoothLEConnection?) {
        super.onNewConnection(mConnection)
        mConnection ?: return

        mWaveformTimer!!.schedule(waveformGUI, 0, 30)
    }

    override fun initialize(): Boolean {
        airProtocol.setProtocolListener(this)
        return true
    }

    @Synchronized override fun deinitialize() {
        super.deinitialize()
        startStreamTimeout?.let {
            it.dispose()
            startStreamTimeout = null
        }

        airProtocol.setProtocolListener(null)

        mWaveformTimer?.let {
            it.cancel()
            mWaveformTimer = null
            mWaveformQueue = null
        }
    }

    override fun onDescriptorWrite() {
        LOG.d("onDescriptorWrite")
        startParameterStream()
    }

    @Synchronized override fun onNewData(bytes: ByteArray) {
        airProtocol.parseData(bytes)
    }

    override fun onRequestReceived(baseUIRequest: BaseUIRequest) {}

    override fun onMessageReceived(message: ProtocolPacket) {
        when (ProtocolUtil.array2short(message.data, 0)) {
            AirConstants.ResponseID.ACK             -> onAck(mTranslator.translateAckMessage(message))
            AirConstants.ResponseID.NAK             -> onNak(mTranslator.translateNakMessage(message))
            AirConstants.ResponseID.PARAMETER_GROUP -> onParameterGroup(mTranslator.translateParameterGroupMessage(message))
            AirConstants.ResponseID.WAVEFORMS       -> onWaveformData(mTranslator.translatePlethSIQWaveformMessage(message, false))
        }
    }

    private fun onWaveformData(event: WaveformDataEvent) {
        var plethData: WaveformData? = null
        var sigIqData: WaveformData? = null

        for (waveformData in event.waveformDTOs) {
            if (waveformData.waveformID == WaveformID.PLETH) {
                plethData = waveformData
            } else if (waveformData.waveformID == WaveformID.SIQ) {
                sigIqData = waveformData
            }
        }

        if (mWaveformQueue == null) return
        // both must be populated
        mWaveformQueue!!.Enqueue(plethData!!.buffer, sigIqData!!.buffer)
    }

    private fun onParameterGroup(dataGroupResponse: DataGroupResponse) {
        //Parameter Info
        callback.onParameterData(AirProtocolParameterData(dataGroupResponse.groupData as ParameterDataGroup))
    }

    private fun onNak(nakResponse: NakResponse) {
        LOG.d(String.format("Nak Received...original RequestType = %s, NakType = %s ", nakResponse.originalAirRequestType.name,
                            nakResponse.nakType.name))
    }

    private fun onAck(ackResponse: AckResponse) {
        LOG.d(String.format("ACK Received...original RequestType = %s", ackResponse.originalAirRequestType.name))
        when (ackResponse.originalAirRequestType) {
            AirRequestType.CONFIGURE_PERIODIC_PARAMETER_GROUP_OUTPUT -> {
                //Start Waveform Stream after parameter Started Successfully
                LOG.d("startPlethSIQWaveformStream()")
                startPlethSIQWaveformStream()
                onStreamStarted()
            }
            AirRequestType.CONFIGURE_PERIODIC_WAVEFORM_OUTPUT        -> {
            }
            else -> {
                // do nothing
            }
        }
    }

    private fun startParameterStream() {
        startStreamTimeout =
            Observable.intervalRange(1, MAX_START_STREAM_ATTEMPTS.toLong(), 0, PARAM_STREAM_TIMEOUT_SECONDS.toLong(),
                                     TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(Schedulers.trampoline())
                .doOnNext { attempt: Long -> tryStartParameterStream(attempt) }
                .doOnComplete { onStartParameterStreamTimeOut() }
                .subscribe({ }, { error: Throwable -> LOG.e(error.message) })
    }

    private fun tryStartParameterStream(attempt: Long) {
        LOG.d(String.format(Locale.ROOT, "Attempt %d to start stream.", attempt))
        val parameterStreamRequest = ConfigurePeriodicParameterGroupRequest(DataGroupType.GROUP_PARAMETER_INFO,
                                                                            PARAMETER_STREAMING_RATE_MS.toShort())
        val parameterStreamCommand = mTranslator.translateConfigurePeriodicParameterGroupRequest(parameterStreamRequest)
        dispatchCommand(parameterStreamCommand)
    }

    private fun onStreamStarted() {
        if (startStreamTimeout == null) return
        startStreamTimeout!!.dispose()
        startStreamTimeout = null
    }

    private fun onStartParameterStreamTimeOut() {
        LOG.d("ConfigurePeriodicParameterGroupRequest remains un-ACK-ed. Giving up.")
        startStreamTimeout = null
        stateListener.onMaxRetries()
    }

    private fun startPlethSIQWaveformStream() {
        val waveformSet =
            EnumSet.of(WaveformSelection.CLIPPED_AUTOSCALE_DATA,
                       WaveformSelection.SIGNAL_IQ_DATA)

        //            waveformSet.add(WaveformSelection.RAW_IR)
        //            waveformSet.add(WaveformSelection.RAW_RED)

        val waveformStreamRequest = ConfigurePeriodicWaveformRequest(waveformSet, WAVEFORM_STREAMING_RATE_MS)
        val waveformStreamCommand = mTranslator.translateConfigurePeriodicWaveformOutputRequest(waveformStreamRequest)
        dispatchCommand(waveformStreamCommand)
    }

    private var waveformGUI: TimerTask = object : TimerTask() {
        override fun run() {
            if (mWaveformQueue!!.count > 0) {
                val waveforms = mWaveformQueue!!.Dequeue()

                waveformCallback?.newValue(System.currentTimeMillis(), waveforms[0], waveforms[1])
            }
        }
    }
}