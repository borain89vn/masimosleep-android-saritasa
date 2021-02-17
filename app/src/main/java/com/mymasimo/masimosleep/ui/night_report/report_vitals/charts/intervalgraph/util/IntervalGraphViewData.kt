package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util

data class IntervalGraphViewData(
    val average: Double,
    val intervals: List<Interval>,
    val timeSpan: TimeSpan
) {
    data class Interval(
        val index: Int,
        val startAt: Long,
        val endAt: Long,
        val values: Set<Double>
    )
}
