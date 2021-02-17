package com.mymasimo.masimosleep.model

enum class SessionType constructor(val value: Int) {

    /**
     * Does not contain Parameter Trend Files (i.e. MightySat Parameter Trend Session/Summary).
     */
    SUMMARY(0),

    /**
     * Contains Parameter Trend Files (i.e. Session created on the device).
     */
    FULL(1),

    /**
     * Contains Parameter Trend Files and Statistics (i.e Heart Rate Recovery Session).
     */
    FULL_HRR(2);

    companion object {
        fun fromValue(value: Int) = when (value) {
            SUMMARY.value -> SUMMARY
            FULL.value -> FULL
            FULL_HRR.value -> FULL_HRR
            else -> throw IllegalArgumentException("Invalid session type value $value")
        }
    }

}
