package com.mymasimo.masimosleep.ui.night_report.report_measurements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ParameterReadingRepository
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.model.LineGraphViewData
import com.mymasimo.masimosleep.model.MeasurementViewData
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ReportMeasurementsViewModel @Inject constructor(
    private val parameterReadingRepository: ParameterReadingRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _measurementsViewData = MutableLiveData(MeasurementViewData())
    val measurementViewData : LiveData<MeasurementViewData>
        get() = _measurementsViewData

    fun onCreate(sessionId: Long) {
        Maybe.zip(getSessionByType(ReadingType.SP02, sessionId),
            getSessionByType(ReadingType.PR, sessionId),
            getSessionByType(ReadingType.RRP, sessionId),
            { oxygenLevel, pulseRate, respirationRate ->
                MeasurementViewData(oxygenLevel, pulseRate, respirationRate)
            })
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())

            .subscribe { measurementViewData ->
                _measurementsViewData.value = measurementViewData

            }.addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun getSessionByType(type: ReadingType, sessionId: Long): Maybe<Double> {
        return parameterReadingRepository.getAllSessionReadings(type, sessionId)
            .filter { it.isNotEmpty() }
            .map { readings ->
                readings.sumByDouble { it.value } / readings.size.toDouble()
            }
    }


}