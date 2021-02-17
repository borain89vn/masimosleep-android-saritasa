package com.mymasimo.masimosleep.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class NightSummaryViewModel @Inject constructor(
        private val sessionRepository: SessionRepository,
        private val sleepScoreRepository: SleepScoreRepository,
        private val eventsRepository: SleepEventRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _summaryViewData = MutableLiveData<SummaryViewData>()
    val summaryViewData: LiveData<SummaryViewData>
        get() = _summaryViewData

    fun onCreated(sessionId: Long) {
        Single.zip(
                sessionRepository.getSessionById(sessionId),
                eventsRepository.getCountOfAllEventsInSession(sessionId),
                sleepScoreRepository.getSessionScore(sessionId)
                    .map { it.value }
                    .onErrorReturn { 0.0 },
                Function3<SessionEntity, Int, Double, SummaryViewData> { session, events, score ->
                    val endAt = session.endAt ?: throw IllegalStateException()
                    val timeSleptMillis = endAt - session.startAt

                    return@Function3 SummaryViewData(
                            timeSleptMinutes = (timeSleptMillis / 1000 / 60).toInt(),
                            numEvents = events,
                            sleepScore = score
                    )
                })
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ viewData ->
                           _summaryViewData.value = viewData
                       }, { it.printStackTrace() })
            .addTo(disposables)
    }

    data class SummaryViewData(
            val timeSleptMinutes: Int,
            val numEvents: Int,
            val sleepScore: Double
    )
}
