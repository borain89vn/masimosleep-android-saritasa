package com.masimo.android.airlib

import com.masimo.common.model.universal.ExceptionID
import com.masimo.common.model.universal.ParameterID
import com.masimo.common.utility.SymmetricSetDiff
import com.masimo.communication.base.model.data.ParameterData
import com.masimo.masimo.protocol.air.model.data.group.ParameterDataGroup

class AirProtocolParameterData(dataGroup: ParameterDataGroup) {
    val spo2: Float
    val pr: Float
    val pi: Float
    val rrp: Float
    val pvi: Float

    val exceptions: Set<ExceptionID> = dataGroup.mSystemExceptions.toSet()

    val spo2Exceptions: Set<ExceptionID>
    val prExceptions: Set<ExceptionID>
    val piExceptions: Set<ExceptionID>
    val rrpExceptions: Set<ExceptionID>
    val pviExceptions: Set<ExceptionID>

    init {
        val map = dataGroup.mParameterInfo.associateBy { it.parameterId }

        spo2 = map.valueOrZero(ParameterID.FUNC_SPO2)
        pr = map.valueOrZero(ParameterID.PR)
        pi = map.valueOrZero(ParameterID.PI)
        rrp = map.valueOrZero(ParameterID.RRP)
        pvi = map.valueOrZero(ParameterID.PVI)

        spo2Exceptions = map.exceptionOrEmptySet(ParameterID.FUNC_SPO2)
        prExceptions = map.exceptionOrEmptySet(ParameterID.PR)
        piExceptions = map.exceptionOrEmptySet(ParameterID.PI)
        rrpExceptions = map.exceptionOrEmptySet(ParameterID.RRP)
        pviExceptions = map.exceptionOrEmptySet(ParameterID.PVI)

    }

    fun diffExceptions(current: Set<ExceptionID>): SymmetricSetDiff<ExceptionID> = SymmetricSetDiff.diff(current, exceptions, ExceptionID::class.java)

    override fun toString(): String =
        "SpO2 = $spo2 + ${spo2Exceptions.joinToString()}, PR = $pr + ${prExceptions.joinToString()} , PI = $pi + ${piExceptions.joinToString()}, RRP = $rrp + ${rrpExceptions.joinToString()}, PVI = $pvi + ${pviExceptions.joinToString()}. Exceptions = [$exceptions]"
}

class AirProtocolWaveformData

private fun Map<ParameterID, ParameterData>.valueOrZero(pid: ParameterID): Float = this[pid]?.value ?: 0f

private fun Map<ParameterID, ParameterData>.exceptionOrEmptySet(pid: ParameterID): Set<ExceptionID> {
    return this[pid]?.parameterExceptions ?: emptySet()
}

interface AirProtocolDataCallback {
    fun onParameterData(params: AirProtocolParameterData)
    fun onWaveformData(waveforms: AirProtocolWaveformData)
}