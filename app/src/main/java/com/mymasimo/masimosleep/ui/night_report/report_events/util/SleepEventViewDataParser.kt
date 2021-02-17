package com.mymasimo.masimosleep.ui.night_report.report_events.util

import com.mymasimo.masimosleep.data.room.entity.SleepEventEntity
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import javax.inject.Inject

class SleepEventViewDataParser @Inject constructor() {

    /**
     * Map a list of [SleepEventEntity]s into a data structure that provides them in hourly ranges
     * from the session start time to the current time.
     */
    fun parseSessionEvents(
        sessionStartAt: Long,
        sessionEndAt: Long,
        events: List<SleepEventEntity>
    ): SleepEventsViewData {
        val intervalWindow = 15 * 60 * 1000 // 15 mins.

        // Create a list of interval start times from the first event in a session start time to the current time. The
        // intervals are [intervalWindow] long and the last one could end beyond the current time.
        var intervalStartAtMillis = events.filter { it.type == SleepEventType.MILD || it.type == SleepEventType.SEVERE }.filter { it.startTime > sessionStartAt && it.endTime < sessionEndAt }.minBy { it.startTime }?.startTime?: sessionStartAt

        val intervalsStartAtMillis = mutableListOf<Long>()

        while (intervalStartAtMillis < sessionEndAt) {

            intervalsStartAtMillis.add(intervalStartAtMillis)

            intervalStartAtMillis += intervalWindow
        }

        val minorEvents = events.filter { it.type == SleepEventType.MILD }
        val majorEvents = events.filter { it.type == SleepEventType.SEVERE }

        // Create a list of interval data. The end time of an interval is 15 mins after the start
        // time (minus 1 millisecond). Find all the events in that range by severity.
        val eventsByInterval = intervalsStartAtMillis.mapIndexed { index, startAt ->
            val endAt = startAt + intervalWindow - 1
            return@mapIndexed SleepEventsViewData.Interval(
                index = index,
                startAt = startAt,
                endAt = endAt,
                minorEvents = minorEvents.count { event -> event.startTime in startAt..endAt },
                majorEvents = majorEvents.count { event -> event.startTime in startAt..endAt }
            )
        }

        return SleepEventsViewData(
            totalEvents = events.size,
            minorEvents = minorEvents.size,
            majorEvents = majorEvents.size,
            eventsByHour = eventsByInterval
        )
    }
}