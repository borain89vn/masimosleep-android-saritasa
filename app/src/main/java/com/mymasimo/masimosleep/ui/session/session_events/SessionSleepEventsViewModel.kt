package com.mymasimo.masimosleep.ui.session.session_events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import com.mymasimo.masimosleep.ui.session.session_events.util.SleepEventViewDataParser
import com.mymasimo.masimosleep.ui.session.session_events.util.SleepEventsViewData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SessionSleepEventsViewModel @Inject constructor(
    private val sleepEventRepository: SleepEventRepository,
    private val sleepEventViewDataParser: SleepEventViewDataParser,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _sleepEvents = MutableLiveData<SleepEventsViewData>()
    val sleepEvents: LiveData<SleepEventsViewData>
        get() = _sleepEvents

    fun onCreate(sessionStartAt: Long) {
        sleepEventRepository.sleepEventUpdates(sessionStartAt)
            .map { events -> sleepEventViewDataParser.parseSessionEvents(sessionStartAt, events) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { sleepEventsViewData ->
                _sleepEvents.value = sleepEventsViewData
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}