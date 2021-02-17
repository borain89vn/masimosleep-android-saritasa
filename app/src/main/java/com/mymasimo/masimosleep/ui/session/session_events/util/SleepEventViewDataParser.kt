package com.mymasimo.masimosleep.ui.session.session_events.util

import com.mymasimo.masimosleep.data.room.entity.SleepEventEntity
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import java.util.*
import javax.inject.Inject

class SleepEventViewDataParser @Inject constructor() {

    /**
     * Map a list of [SleepEventEntity]s into a data structure that provides them in hourly ranges
     * from the session start time to the current time.
     */
    fun parseSessionEvents(
        sessionStartAt: Long,
        events: List<SleepEventEntity>
    ): SleepEventsViewData {
        val hourMillis = 60 * 60 * 1000 // 1 hour.
        val now = Calendar.getInstance().timeInMillis

        // Create a list of interval start times from the session start to the current time. The
        // intervals are 1 hours long and the last one could end beyond the current time.
        var intervalStartAtMillis = sessionStartAt
        val intervalsStartAtMillis = mutableListOf<Long>()
        while (intervalStartAtMillis < now) {
            intervalsStartAtMillis.add(intervalStartAtMillis)
            intervalStartAtMillis += hourMillis
        }

        val minorEvents = events.filter { it.type == SleepEventType.MILD }
        val majorEvents = events.filter { it.type == SleepEventType.SEVERE }

        // Create a list of interval data. The end time of an interval is 1 hour after the start
        // time (minus 1 millisecond). Find all the events in that range by severity.
        val eventsByHours = intervalsStartAtMillis.mapIndexed { index, startAt ->
            val endAt = startAt + hourMillis - 1
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
            eventsByHour = eventsByHours
        )
    }
}