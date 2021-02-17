package com.mymasimo.masimosleep.ui.night_report.report_sleep_quality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ReportSleepQualityViewModel @Inject constructor(
        private val scoreRepository: SleepScoreRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposable: CompositeDisposable
) : ViewModel() {

    private val _sessionScore = MutableLiveData<Double>()
    val sessionScore: LiveData<Double>
        get() = _sessionScore

    fun onCreate(sessionId: Long) {
        scoreRepository.getSessionScore(sessionId)
            .map { it.value }
            .onErrorReturnItem(0.0)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ score ->
                           _sessionScore.value = score
                       }, { it.printStackTrace() })
            .addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}
