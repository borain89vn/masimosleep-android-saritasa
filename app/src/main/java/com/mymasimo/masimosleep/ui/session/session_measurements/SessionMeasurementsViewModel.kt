package com.mymasimo.masimosleep.ui.session.session_measurements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ParameterReadingRepository
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.model.MeasurementViewData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SessionMeasurementsViewModel @Inject constructor(
    private val parameterReadingRepository: ParameterReadingRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {



    private val _currentReadingSP02 = MutableLiveData<Double>()
    val currentReadingSP02: LiveData<Double>
        get() = _currentReadingSP02

    private val _currentReadingPR = MutableLiveData<Double>()
    val currentReadingPR: LiveData<Double>
        get() = _currentReadingPR

    private val _currentReadingRRP = MutableLiveData<Double>()
    val currentReadingRRP: LiveData<Double>
        get() = _currentReadingRRP

    fun onCreate(sessionStartAt: Long) {
        getLiveReadingSource(ReadingType.SP02, sessionStartAt)
        getLiveReadingSource(ReadingType.PR, sessionStartAt)
        getLiveReadingSource(ReadingType.RRP, sessionStartAt)

        Single.zip( parameterReadingRepository.latestSPO2Reading(),
            parameterReadingRepository.latestPRReading(),
            parameterReadingRepository.latestRRPReading(),
            { oxygenLevel, pulseRate, respirationRate ->
                MeasurementViewData(oxygenLevel, pulseRate, respirationRate)
            })
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())

            .subscribe { measurementViewData ->
                _currentReadingSP02.value = measurementViewData.oxygen_level
                _currentReadingPR.value = measurementViewData.pulse_rate
                _currentReadingRRP.value = measurementViewData.respiratory_rate

            }.addTo(disposables)


    }




    private fun getLiveReadingSource(readingType: ReadingType, sessionStartAt: Long){
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
                when (readingType) {
                    ReadingType.SP02 -> _currentReadingSP02.value = liveReading
                    ReadingType.PR   -> _currentReadingPR.value = liveReading
                    ReadingType.RRP  -> _currentReadingRRP.value = liveReading
                }
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}