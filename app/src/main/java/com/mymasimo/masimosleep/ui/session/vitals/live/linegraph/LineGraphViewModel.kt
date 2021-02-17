package com.mymasimo.masimosleep.ui.session.vitals.live.linegraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ParameterReadingRepository
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.ui.session.vitals.live.linegraph.util.LineGraphViewData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class LineGraphViewModel @Inject constructor(
    private val parameterReadingRepository: ParameterReadingRepository,
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
        // The final point data structure should be a list of list of points. Each list represents
        // a continuous block of points without interruption. E.g.
        // points = [
        //     [ reading1, reading2 ],
        //     [ reading3, reading4, reading5 ],
        // ]
        // In the example, reading2 and reading3 have creation dates whose diff exceeds beyond
        // the block threshold (currently 2 minutes) - so we finalize the first block and create a
        // new one.
        val points = mutableListOf<List<LineGraphViewData.LineGraphPoint>>()
        val currentPointBlock = mutableListOf<LineGraphViewData.LineGraphPoint>()
        var lastReadingMillis = readings.first().createdAt
        readings.forEach { reading ->
            val readingsDiffMillis = reading.createdAt - lastReadingMillis
            if (readingsDiffMillis > BLOCK_SEPARATION_THRESHOLD_MILLIS) {
                points.add(currentPointBlock.toList())
                currentPointBlock.clear()
            }
            currentPointBlock.add(LineGraphViewData.LineGraphPoint(
                value = reading.value,
                timestamp = reading.createdAt
            ))
            lastReadingMillis = reading.createdAt
        }
        points.add(currentPointBlock.toList())

        return LineGraphViewData(
            average = readings.sumByDouble { it.value } / readings.size.toDouble(),
            points = points
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    companion object {
        private const val BLOCK_SEPARATION_THRESHOLD_MILLIS = 3 * 60 * 1000 // 3 minutes.
    }
}