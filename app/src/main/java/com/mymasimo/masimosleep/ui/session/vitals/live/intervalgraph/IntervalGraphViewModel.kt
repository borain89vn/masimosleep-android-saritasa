package com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ParameterReadingRepository
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.util.Interval
import com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.util.IntervalGraphViewData
import com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.util.TimeSpan
import com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.util.toMillis
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class IntervalGraphViewModel @Inject constructor(
        private val parameterReadingRepository: ParameterReadingRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _intervalGraphViewData = MutableLiveData<IntervalGraphViewData>()
    val intervalGraphViewData: LiveData<IntervalGraphViewData>
        get() = _intervalGraphViewData

    private val _currentReading = MutableLiveData<Double>()
    val currentReading: LiveData<Double>
        get() = _currentReading

    fun onCreate(readingType: ReadingType, sessionStartAt: Long, interval: Interval, timeSpan: TimeSpan) {
        val liveReadingSource = when (readingType) {
            ReadingType.SP02 -> parameterReadingRepository.liveSpO2Reading
            ReadingType.PR   -> parameterReadingRepository.livePrReading
            ReadingType.RRP  -> parameterReadingRepository.liveRrpReading
            else             -> Observable.just(0.0)
        }
        liveReadingSource
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { liveReading ->
                _currentReading.value = liveReading
            }
            .addTo(disposables)

        parameterReadingRepository.getAllReadingsUpdates(readingType, sessionStartAt)
            .filter { it.isNotEmpty() }
            .map { readings -> parseReadingIntoViewData(sessionStartAt, readings, interval, timeSpan) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { intervalGraphViewData ->
                _intervalGraphViewData.value = intervalGraphViewData
                Timber.i("Interval graph data: $intervalGraphViewData")
            }
            .addTo(disposables)
    }

    private fun parseReadingIntoViewData(
            sessionStartAt: Long,
            readings: List<ParameterReadingEntity>,
            interval: Interval,
            timeSpan: TimeSpan
    ): IntervalGraphViewData {
        val nowMillis = Calendar.getInstance().timeInMillis

        var intervalStartAtMillis = sessionStartAt
        val intervalsStartAtMillis = mutableListOf<Long>()
        while (intervalStartAtMillis < nowMillis) {
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