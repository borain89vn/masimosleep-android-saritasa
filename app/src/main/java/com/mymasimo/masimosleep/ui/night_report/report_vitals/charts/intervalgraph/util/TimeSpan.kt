package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util

data class TimeSpan(
    val minutes: Int
)

fun TimeSpan.toMillis(): Long {
    return minutes * 60 * 1000L
}


