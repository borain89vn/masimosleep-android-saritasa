package com.mymasimo.masimosleep.data.repository

import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.ParameterReadingEntityDao
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParameterReadingRepository @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val parameterReadingDao: ParameterReadingEntityDao,
    private val schedulerProvider: SchedulerProvider,
    private val disposable: CompositeDisposable
) {

    private val spO2Buffer = mutableListOf<Float>()
    private val prBuffer = mutableListOf<Float>()
    private val rrpBuffer = mutableListOf<Float>()

    private val liveSpO2ReadingRelay = PublishRelay.create<Double>()
    val liveSpO2Reading: Observable<Double>
        get() = liveSpO2ReadingRelay

    private val livePrReadingRelay = PublishRelay.create<Double>()
    val livePrReading: Observable<Double>
        get() = livePrReadingRelay

    private val liveRrpReadingRelay = PublishRelay.create<Double>()
    val liveRrpReading: Observable<Double>
        get() = liveRrpReadingRelay

    /**
     * Returns the latest SPO2 reading from the database.
     */
    fun latestSPO2Reading(): Single<Double> {
        return parameterReadingDao.latestSPO2Reading()
            .map { it.value }
            .toSingle(0.0)
    }

    /**
     * Returns updates with the readings of the [type] that were created after [startAt].
     * Note: these readings update every 1 minute since we do batch inserts every minute.
     */
    fun getAllReadingsUpdates(
        type: ReadingType,
        startAt: Long
    ): Observable<List<ParameterReadingEntity>> {
        return parameterReadingDao.findAllByTypeAfterTimestampUpdating(type, startAt)
    }

    fun getAllSessionReadings(
        type: ReadingType,
        sessionId: Long
    ): Single<List<ParameterReadingEntity>> {
        return sessionRepository.getSessionById(sessionId)
            .flatMap { session ->
                val endAt = session.endAt
                    ?: return@flatMap Single.error<List<ParameterReadingEntity>>(
                        IllegalStateException("Session doesn't have endAt")
                    )
                return@flatMap parameterReadingDao.findAllByTypeBetweenTimestamps(
                    type, session.startAt, endAt)
            }

    }

    fun saveReading(type: ReadingType, value: Float, insertWithBatch: Boolean = true) {
        // Notify the live relays first in case there's observers interested in live readings.
        when (type) {
            ReadingType.SP02 -> liveSpO2ReadingRelay.accept(value.toDouble())
            ReadingType.PR -> livePrReadingRelay.accept(value.toDouble())
            ReadingType.RRP -> liveRrpReadingRelay.accept(value.toDouble())
        }

        // Save the reading to the database - either in batch every 1 minute or immediately.
        synchronized(this) {
            if (!insertWithBatch) {
                insertReading(
                    reading = ParameterReadingEntity(
                        type = type,
                        value = value.toDouble(),
                        dataPointCount = 1,
                        createdAt = Calendar.getInstance().timeInMillis
                    ),
                    isAverage = false
                )
                return
            }

            // Do batch insert.
            when (type) {
                ReadingType.SP02 -> {
                    spO2Buffer.add(value)
                    // If there was already a value in the buffer then the timer should already be
                    // running.
                    if (spO2Buffer.size == 1) {
                        startInsertTimer(type)
                    }
                }
                ReadingType.PR -> {
                    prBuffer.add(value)
                    // If there was already a value in the buffer then the timer should already be
                    // running.
                    if (prBuffer.size == 1) {
                        startInsertTimer(type)
                    }
                }
                ReadingType.RRP -> {
                    rrpBuffer.add(value)
                    // If there was already a value in the buffer then the timer should already be
                    // running.
                    if (rrpBuffer.size == 1) {
                        startInsertTimer(type)
                    }
                }
            }
        }
    }

    private fun startInsertTimer(type: ReadingType) {
        Timber.d("Starting 1 minute timer to persist ${type.key} readings")
        Observable.timer(1, TimeUnit.MINUTES)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.io())
            .subscribe {
                onTimerComplete(type)
            }
            .addTo(disposable)
    }

    private fun onTimerComplete(type: ReadingType) {
        synchronized(this) {
            Timber.d("Timer expired, averaging buffer of ${type.key} readings to persist to the DB")
            val avgReading = mergeBufferReadings(type)
            clearBuffer(type)
            insertReading(avgReading, isAverage = true)
        }
    }

    private fun insertReading(reading: ParameterReadingEntity, isAverage: Boolean) {
        parameterReadingDao.insert(reading)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (isAverage) {
                    Timber.d("Averaged reading of ${reading.type.key} inserted")
                } else {
                    Timber.d("Single reading of ${reading.type.key} inserted")
                }
            }
            .addTo(disposable)
    }

    private fun mergeBufferReadings(type: ReadingType): ParameterReadingEntity {
        val buffer = when (type) {
            ReadingType.SP02 -> spO2Buffer
            ReadingType.PR -> prBuffer
            ReadingType.RRP -> rrpBuffer
            else -> listOf<Float>()
        }
        val average = buffer.sumByDouble { it.toDouble() } / buffer.size

        Timber.d("Calculated average of ${type.key}=$average, from ${buffer.size} data points")
        return ParameterReadingEntity(
            type = type,
            value = average,
            dataPointCount = buffer.size,
            createdAt = Calendar.getInstance().timeInMillis
        )
    }

    private fun clearBuffer(type: ReadingType) {
        when (type) {
            ReadingType.SP02 -> spO2Buffer.clear()
            ReadingType.PR -> prBuffer.clear()
            ReadingType.RRP -> rrpBuffer.clear()
        }
    }
}
