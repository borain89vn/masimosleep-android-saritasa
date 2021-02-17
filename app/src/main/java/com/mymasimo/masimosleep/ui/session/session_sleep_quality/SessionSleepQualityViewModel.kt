package com.mymasimo.masimosleep.ui.session.session_sleep_quality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject


class SessionSleepQualityViewModel @Inject constructor(
    private val sleepScoreRepository: SleepScoreRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _liveScore = MutableLiveData<Double>()
    val liveScore: LiveData<Double>
        get() = _liveScore

    fun onCreate(sessionStartAt: Long) {
        sleepScoreRepository.recentLiveScoreUpdates(sessionStartAt)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { liveScore ->
                _liveScore.value = liveScore
            }.addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()

        disposables.clear()
    }

}