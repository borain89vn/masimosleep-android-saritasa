package com.mymasimo.masimosleep.ui.night_report.report_bed_time

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ReportTimeInBedViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposable: CompositeDisposable
) : ViewModel() {

    private val _sleepRange = MutableLiveData<LongRange>()
    val sleepRange: LiveData<LongRange>
        get() = _sleepRange

    fun onCreated(sessionId: Long) {
        sessionRepository.getSessionById(sessionId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { session ->
                val endAt = session.endAt ?: throw IllegalStateException()
                _sleepRange.value = LongRange(session.startAt, endAt)
            }
            .addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}
