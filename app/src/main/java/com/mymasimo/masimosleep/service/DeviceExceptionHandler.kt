package com.mymasimo.masimosleep.service

import com.jakewharton.rxrelay2.BehaviorRelay
import com.masimo.common.model.universal.ExceptionID
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceExceptionHandler @Inject constructor() {

    private val _exceptionUpdates = BehaviorRelay.create<Set<DeviceException>>()
    val exceptionUpdates: Observable<Set<DeviceException>>
        get() = _exceptionUpdates

    private val _exceptions: MutableSet<DeviceException> = mutableSetOf()

    @Synchronized
    fun updateExceptions(exceptions: Set<ExceptionID>) {
        //Append exceptions to current exceptions
        exceptions.forEach { exception ->
            when (exception) {
                ExceptionID.SENSOR_OFF_PATIENT -> {
                    _exceptions.add(DeviceException.SENSOR_OFF_PATIENT)
                }
                ExceptionID.DEFECTIVE_SENSOR   -> {
                    _exceptions.add(DeviceException.DEFECTIVE_SENSOR)
                }
                ExceptionID.LOW_BATTERY        -> {
                    _exceptions.add(DeviceException.LOW_BATTERY)
                }
                else                           -> { /* We don't care about other exception types. */
                }
            }
        }

        clearCertainExceptionsIfNotExistAnymore(exceptions)

        Timber.d("Device exceptions: $_exceptions")
        val deviceExceptions = _exceptions.toMutableSet()
        _exceptionUpdates.accept(deviceExceptions)
    }

    @Synchronized
    private fun clearCertainExceptionsIfNotExistAnymore(exceptions: Set<ExceptionID>) {
        /**
         * DeviceException.SENSOR_OFF_PATIENT is purposely left out
         */
        if (_exceptions.contains(DeviceException.DEFECTIVE_SENSOR) && !exceptions.contains(ExceptionID.DEFECTIVE_SENSOR)) {
            _exceptions.remove(DeviceException.DEFECTIVE_SENSOR)
        }
        if (_exceptions.contains(DeviceException.LOW_BATTERY) && !exceptions.contains(ExceptionID.LOW_BATTERY)) {
            _exceptions.remove(DeviceException.LOW_BATTERY)
        }
    }

    @Synchronized
    fun clearExceptions() {
        _exceptions.clear()
        val deviceExceptions = _exceptions.toMutableSet()
        _exceptionUpdates.accept(deviceExceptions)
    }

    @Synchronized
    fun clearSensorOffException() {
        _exceptions.remove(DeviceException.SENSOR_OFF_PATIENT)
        val deviceExceptions = _exceptions.toMutableSet()
        _exceptionUpdates.accept(deviceExceptions)
    }
}

enum class DeviceException {
    SENSOR_OFF_PATIENT, DEFECTIVE_SENSOR, LOW_BATTERY
}