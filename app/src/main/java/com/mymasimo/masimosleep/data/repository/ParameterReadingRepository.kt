package com.mymasimo.masimosleep.data.repository

import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.ParameterReadingEntityDao
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.data.room.entity.ReadingWithTimestamp
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
    private val disposable: CompositeDisposable,
    private val rawParameterReadingRepository: RawParameterReadingRepository,
) {

    private val readingsBuffer = mutableListOf<ReadingWithTimestamp>()

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
     * Returns the latest RP reading from the database.
     */
    fun latestPRReading(): Single<Double> {
        return parameterReadingDao.latestPRReading()
            .map { it.value }
            .toSingle(0.0)
    }

    /**
     * Returns the latest RPR reading from the database.
     */
    fun latestRRPReading(): Single<Double> {
        return parameterReadingDao.latestRPRReading()
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
                    ParameterReadingEntity(
                        type = type,
                        value = value.toDouble(),
                        dataPointCount = 1,
                        createdAt = Calendar.getInstance().timeInMillis
                    ),
                    isAverage = false
                )
                return
            }

            val currentReading = ReadingWithTimestamp(
                type = type,
                value = value,
                timestamp = Calendar.getInstance().timeInMillis
            )

            // Accumulate the data in buffer
            readingsBuffer.add(currentReading)
            // If there was already a value in the buffer then the timer should already be
            // running.
            if (readingsBuffer.size == 1) {
                 startInsertTimer()
            }
        }
    }

    private fun startInsertTimer() {
        Timber.d("Starting 1 minute timer to persist readings")
        Observable.timer(1, TimeUnit.MINUTES)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.io())
            .subscribe {
                onTimerComplete()
            }
            .addTo(disposable)
    }

    private fun onTimerComplete() {
        synchronized(this) {
                Timber.d("Timer expired, averaging buffer of readings to persist to the DB")
            val avgReadings = mergeBufferReadings()
            saveRawReadingData()
            insertReading(*avgReadings.toTypedArray(), isAverage = true)
            clearBuffer()
        }
    }

    /**
     * Save all sensor reading data.
     *
     * Sensor data is received with 1Hz frequency.
     * Every minute it is aggregated, the result stored in the DB for charts purposes and removed from buffer.
     * So we need to persist the data before the buffer is cleaned.
     */
    private fun saveRawReadingData() {
        rawParameterReadingRepository.saveRawReadingData(readingsBuffer)
    }

    private fun insertReading(vararg readings: ParameterReadingEntity, isAverage: Boolean) {
        parameterReadingDao.insert(*readings)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (isAverage) {
                    Timber.d("Averaged readings inserted")
                } else {
                    Timber.d("Single reading of ${readings.firstOrNull()?.type?.key} inserted")
                }
            }
            .addTo(disposable)
    }

    private fun mergeBufferReadings(): List<ParameterReadingEntity> {
        val mergedEntities = mutableListOf<ParameterReadingEntity>()
        for (type in ReadingType.values()) {
            if (type == ReadingType.DEFAULT) continue
            val readings = readingsBuffer.filter { it.type == type }
            val average = readings.map { it.value.toDouble() }.average()
            Timber.d("Calculated average of ${type.key}=$average, from ${readings.size} data points")
            mergedEntities.add(
                ParameterReadingEntity(
                    type = type,
                    value = average,
                    dataPointCount = readings.size,
                    createdAt = Calendar.getInstance().timeInMillis
                )
            )
        }
        return mergedEntities
    }

    private fun clearBuffer() {
        readingsBuffer.clear()
    }
}
