package com.mymasimo.masimosleep.ui.night_report.report_events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import com.mymasimo.masimosleep.ui.night_report.report_events.util.SleepEventViewDataParser
import com.mymasimo.masimosleep.ui.night_report.report_events.util.SleepEventsViewData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ReportEventsViewModel @Inject constructor(
    private val eventsRepository: SleepEventRepository,
    private val sessionRepository: SessionRepository,
    private val sleepEventViewDataParser: SleepEventViewDataParser,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _sleepEvents = MutableLiveData<SleepEventsViewData>()
    val sleepEvents: LiveData<SleepEventsViewData>
        get() = _sleepEvents

    fun onCreated(sessionId: Long) {
        eventsRepository.getAllEventsInSession(sessionId)
            .flatMap { events ->
                sessionRepository.getSessionById(sessionId)
                    .map { session ->
                        val endAt = session.endAt ?: throw IllegalStateException()
                        sleepEventViewDataParser.parseSessionEvents(
                            sessionStartAt = session.startAt,
                            sessionEndAt = endAt,
                            events = events
                        )
                    }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { sleepEventsViewData ->
                _sleepEvents.value = sleepEventsViewData.copy(20,20,10)
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
