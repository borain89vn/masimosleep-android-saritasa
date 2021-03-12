package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph.util

data class LineGraphViewData(
    val average: Double,
    val points: List<List<LineGraphPoint>>
) {
    data class LineGraphPoint(
        val value: Double,
        val timestamp: Long
    )
}
