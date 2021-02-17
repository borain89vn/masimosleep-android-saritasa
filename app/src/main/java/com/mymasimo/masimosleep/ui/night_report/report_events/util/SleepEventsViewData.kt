package com.mymasimo.masimosleep.ui.night_report.report_events.util

data class SleepEventsViewData(
    val totalEvents: Int,
    val minorEvents: Int,
    val majorEvents: Int,
    val eventsByHour: List<Interval>
) {
    data class Interval(
        val index: Int,
        val startAt: Long,
        val endAt: Long,
        val minorEvents: Int,
        val majorEvents: Int
    )
}