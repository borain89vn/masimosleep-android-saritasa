package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ParameterReadingRepository
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.model.LineGraphViewData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ReportLineGraphViewModel @Inject constructor(
    private val parameterReadingRepository: ParameterReadingRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _lineGraphViewData = MutableLiveData<LineGraphViewData>()
    val lineGraphViewData: LiveData<LineGraphViewData>
        get() = _lineGraphViewData

    fun onCreate(readingType: ReadingType, sessionId: Long) {
        parameterReadingRepository.getAllSessionReadings(readingType, sessionId)
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