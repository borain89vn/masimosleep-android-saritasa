package com.mymasimo.masimosleep.ui.night_report.sleep_pattern.util

data class SleepPatternViewData(
    val lowMinutes: Int,
    val highMinutes: Int,
    val avgSleepStartAt: Long,
    val avgSleepEndAt: Long,
    val mostLateEndAt: Long,
    val sleepSessions: List<SleepSession>
) {
    data class SleepSession(
        val night: Int,
        val startAt: Long,
        val endAt: Long
    )
}
