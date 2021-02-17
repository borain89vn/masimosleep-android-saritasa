package com.mymasimo.masimosleep.ui.dashboard.sleeping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.util.test.FakeTicker
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SleepSessionViewModel @Inject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val liveScoreDisposable: CompositeDisposable,
        private val disposables: CompositeDisposable,
        private val sleepSessionScoreManager: SleepSessionScoreManager,
        private val sleepScoreRepository: SleepScoreRepository,
        private val fakeTicker: FakeTicker
) : ViewModel() {
    private val _liveScore = MutableLiveData<Double>()
    val liveScore: LiveData<Double>
        get() = _liveScore

    fun onStartSessionClick() {
        val startedAt = sleepSessionScoreManager.startSession(1)
        startUpdatingLiveScore(startedAt)

        if (FAKE_TICKS_ENABLED) {
            fakeTicker.startFakeTicking()
        }
    }

    fun onEndSessionClick() {
        sleepSessionScoreManager.endSession(null)
        liveScoreDisposable.clear()
    }

    private fun startUpdatingLiveScore(startAt: Long) {
        liveScoreDisposable.clear()
        sleepScoreRepository.recentLiveScoreUpdates(startAt)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { liveScore ->
                _liveScore.value = liveScore
            }
            .addTo(liveScoreDisposable)
    }

    override fun onCleared() {
        liveScoreDisposable.clear()
        disposables.clear()
        super.onCleared()
    }

    companion object {
        private const val FAKE_TICKS_ENABLED = false
    }
}
