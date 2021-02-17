package com.mymasimo.masimosleep.ui.session.vitals.live.linegraph.util

data class LineGraphViewData(
    val average: Double,
    val points: List<List<LineGraphPoint>>
) {
    data class LineGraphPoint(
        val value: Double,
        val timestamp: Long
    )
}
