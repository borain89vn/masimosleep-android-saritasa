package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util

data class Interval(
    val minutes: Int
)

fun Interval.toMillis(): Long {
    return minutes * 60 * 1000L
}


