package com.mymasimo.masimosleep.ui.session.sleep_quality_trend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.room.entity.ScoreEntity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SleepQualityTrendViewModel @Inject constructor(
    private val sleepScoreRepository: SleepScoreRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _viewData = MutableLiveData<SleepQualityTrendViewData>()
    val viewData: LiveData<SleepQualityTrendViewData>
        get() = _viewData

    fun onCreated(sessionStartAt: Long) {
        Observable.interval(0, 10, TimeUnit.MINUTES)
            .flatMap {
                sleepScoreRepository.getAllLiveScoresFrom(sessionStartAt).toObservable()
            }
            .filter { scores -> scores.isNotEmpty() }
            .map { scores -> parseScoresToViewData(scores, sessionStartAt) }
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

    private fun parseScoresToViewData(
        scores: List<ScoreEntity>,
        sessionStartAt: Long
    ): SleepQualityTrendViewData {
        val nowMillis = Calendar.getInstance().timeInMillis

        var intervalStartAtMillis = sessionStartAt
        val intervalsStartAtMillis = mutableListOf<Long>()
        while (intervalStartAtMillis < nowMillis) {
            intervalsStartAtMillis.add(intervalStartAtMillis)
            intervalStartAtMillis += HOUR_MILLIS
        }

        val intervals = intervalsStartAtMillis.mapIndexed { index,  startAt ->
            val endAt = startAt + HOUR_MILLIS - 1

            // Use the scores in the past half hour and future half hour from the interval start at
            // to calculate and average score.
            val avgStartAt = startAt - (HOUR_MILLIS / 2)
            val avgEndAt = startAt + (HOUR_MILLIS / 2)
            val scoresInInterval = scores.filter { score ->
                score.createdAt in avgStartAt..avgEndAt
            }

            return@mapIndexed SleepQualityTrendViewData.Interval(
                index = index,
                startAt = startAt,
                endAt = endAt,
                score = scoresInInterval.sumByDouble { it.value } / scoresInInterval.size.toDouble()
            )
        }

        return SleepQualityTrendViewData(intervals)
    }

    data class SleepQualityTrendViewData(
        val intervals: List<Interval>
    ) {
        data class Interval(
            val index: Int,
            val startAt: Long,
            val endAt: Long,
            val score: Double
        )
    }

    companion object {
        private const val HOUR_MILLIS = 1 * 60 * 60 * 1000 // 1 hour.
    }
}
