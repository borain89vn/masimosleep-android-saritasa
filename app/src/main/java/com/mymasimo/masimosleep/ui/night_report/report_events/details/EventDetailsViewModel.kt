package com.mymasimo.masimosleep.ui.night_report.report_events.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import com.mymasimo.masimosleep.data.room.entity.SleepEventEntity
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class EventDetailsViewModel @Inject constructor(
        private val eventsRepository: SleepEventRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _viewData = MutableLiveData<EventDetailViewData>()
    val viewData: LiveData<EventDetailViewData>
        get() = _viewData

    fun onCreated(sessionId: Long) {
        eventsRepository.getAllEventsInSession(sessionId)
            .map { events -> parseEventsIntoDetailViewData(events) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewData ->
                _viewData.value = viewData
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun parseEventsIntoDetailViewData(events: List<SleepEventEntity>): EventDetailViewData {
        return EventDetailViewData(
                totalEvents = events.size,
                minorEvents = events.count { it.type == SleepEventType.MILD },
                majorEvents = events.count { it.type == SleepEventType.SEVERE },
                events = events.mapIndexed { index, event ->
                    EventDetailViewData.EventSummary(
                            index = index + 1,
                            startTime = event.startTime,
                            duration = event.endTime - event.startTime,
                            spO2 = event.spO2,
                            severity = event.type
                    )
                }
        )
    }

    data class EventDetailViewData(
            val totalEvents: Int,
            val minorEvents: Int,
            val majorEvents: Int,
            val events: List<EventSummary>
    ) {
        data class EventSummary(
                val index: Int,
                val startTime: Long,
                val duration: Long,
                val spO2: Double,
                val severity: SleepEventType
        )
    }
}