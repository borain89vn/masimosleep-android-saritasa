package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ParameterReadingRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.Interval
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.IntervalGraphViewData
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.TimeSpan
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.toMillis
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ReportIntervalGraphViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val parameterReadingRepository: ParameterReadingRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _intervalGraphViewData = MutableLiveData<IntervalGraphViewData>()
    val intervalGraphViewData: LiveData<IntervalGraphViewData>
        get() = _intervalGraphViewData

    fun onCreate(readingType: ReadingType, sessionId: Long, interval: Interval, timeSpan: TimeSpan) {
        parameterReadingRepository.getAllSessionReadings(readingType, sessionId)
            .filter { it.isNotEmpty() }
            .toSingle(emptyList())
            .flatMap { readings ->
                return@flatMap sessionRepository.getSessionById(sessionId)
                    .map { session ->
                        val endAt = session.endAt ?: throw IllegalStateException()
                        parseReadingIntoViewData(session.startAt, endAt, readings, interval, timeSpan)
                    }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { intervalGraphViewData ->
                _intervalGraphViewData.value = intervalGraphViewData
            }
            .addTo(disposables)
    }

    private fun parseReadingIntoViewData(
        sessionStartAt: Long,
        sessionEndAt: Long,
        readings: List<ParameterReadingEntity>,
        interval: Interval,
        timeSpan:TimeSpan
    ): IntervalGraphViewData {
        var intervalStartAtMillis = sessionStartAt
        val intervalsStartAtMillis = mutableListOf<Long>()
        while (intervalStartAtMillis < sessionEndAt) {
            intervalsStartAtMillis.add(intervalStartAtMillis)
            intervalStartAtMillis += interval.toMillis()
        }

        val readingsByInterval = intervalsStartAtMillis.mapIndexed { index, startAt ->
            val endAt = startAt + interval.toMillis() - 1
            return@mapIndexed IntervalGraphViewData.Interval(
                index = index,
                startAt = startAt,
                endAt = endAt,
                values = readings
                   .filter { reading -> reading.createdAt in startAt..endAt }
                   .map { reading -> reading.value }
                   .toSet()
            )
        }

        return IntervalGraphViewData(
            average = readings.sumByDouble { it.value } / readings.size.toDouble(),
            intervals = readingsByInterval,
            timeSpan = timeSpan
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}