package com.mymasimo.masimosleep.ui.session.vitals.live.linegraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import com.mymasimo.masimosleep.data.room.entity.SleepEventEntity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SessionVitalsViewModel @Inject constructor(
    private val eventsRepository: SleepEventRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel(){

    private val _sleepEvents = MutableLiveData<List<SleepEventEntity>>()
    val sleepEvents: LiveData<List<SleepEventEntity>>
        get() = _sleepEvents

    fun onCreate(startAt: Long) {
        eventsRepository.sleepEventUpdates(startAt)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                _sleepEvents.value = it
            }.addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}