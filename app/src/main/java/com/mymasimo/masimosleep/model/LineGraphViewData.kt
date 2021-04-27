package com.mymasimo.masimosleep.model

data class LineGraphViewData(
    val average: Double,
    val points: List<LineGraphPoint>
) {
    data class LineGraphPoint(
        val value: Double,
        val timestamp: Long
    )
}