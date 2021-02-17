package com.mymasimo.masimosleep.util

import com.masimo.common.model.universal.ExceptionID
import java.util.*

object ExceptionMaskUtil {

    /**
     * Converts a Set of ExceptionIds into an int. Non-Supported Exceptions are ignored and will be
     * skipped from the bitmask conversion.
     *
     * @param parameterExceptions
     * @return
     */
    fun convertExceptionToMask(parameterExceptions: Set<ExceptionID>?): Int {
        var value = 0
        if (parameterExceptions != null) {
            for (exceptionID in parameterExceptions) {
                //Only Supported Exceptions
                SUPPORTED_PARAMETER_EXCEPTION_MASK_MAP[exceptionID]?.let {
                    value = value or it
                }
            }
        }
        return value
    }

    /**
     * Converts an int mask into a Set of ExceptionIds. Only App Supported Exceptions will be returned.
     *
     * @param mask
     * @return
     */
    fun convertMaskToExceptionSet(mask: Int): Set<ExceptionID> {
        val exceptionIDSet = EnumSet.noneOf(ExceptionID::class.java)
        //Only Supported Exceptions
        SUPPORTED_PARAMETER_EXCEPTION_MASK_MAP.forEach {
            if (mask and it.value == it.value) {
                exceptionIDSet.add(it.key)
            }
        }

        return exceptionIDSet
    }
}
