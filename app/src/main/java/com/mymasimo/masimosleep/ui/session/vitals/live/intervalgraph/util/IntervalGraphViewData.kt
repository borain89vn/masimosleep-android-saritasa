package com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.util

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
