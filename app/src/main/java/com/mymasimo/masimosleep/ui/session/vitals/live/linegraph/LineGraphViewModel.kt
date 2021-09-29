package com.mymasimo.masimosleep.ui.session.vitals.live.linegraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ParameterReadingRepository
import com.mymasimo.masimosleep.data.repository.RawParameterReadingRepository
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.model.LineGraphViewData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

// TODO() :: rawParameterReadingRepository
class LineGraphViewModel @Inject constructor(
    private val parameterReadingRepository: ParameterReadingRepository,
    private val rawParameterReadingRepository: RawParameterReadingRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _lineGraphViewData = MutableLiveData<LineGraphViewData>()
    val lineGraphViewData: LiveData<LineGraphViewData>
        get() = _lineGraphViewData

    private val _currentReading = MutableLiveData<Double>()
    val currentReading: LiveData<Double>
        get() = _currentReading

    fun onCreate(readingType: ReadingType, sessionStartAt: Long) {
        val liveReadingSource = when (readingType) {
            ReadingType.SP02 -> parameterReadingRepository.liveSpO2Reading
            ReadingType.PR -> parameterReadingRepository.livePrReading
            ReadingType.RRP -> parameterReadingRepository.liveRrpReading
            else -> Observable.just(0.0)
        }
        liveReadingSource
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { liveReading ->
                _currentReading.value = liveReading
            }
            .addTo(disposables)

        parameterReadingRepository.getAllReadingsUpdates(readingType, sessionStartAt)
            .filter { it.isNotEmpty()}
            .map { readings -> parseReadingIntoViewData(readings) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { lineGraphData ->
                _lineGraphViewData.value = lineGraphData
            }
            .addTo(disposables)
    }

    private fun parseReadingIntoViewData(
        readings: List<ParameterReadingEntity>
    ): LineGraphViewData {
        return LineGraphViewData(
            average = readings.sumByDouble { it.value } / readings.size.toDouble(),
            points = readings.map { LineGraphViewData.LineGraphPoint(it.value, it.createdAt) }
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}