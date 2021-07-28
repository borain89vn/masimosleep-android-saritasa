package com.mymasimo.masimosleep.data.repository

import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingEntity
import com.mymasimo.masimosleep.data.room.dao.RawParameterReadingEntityDao
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.reactivex.rxkotlin.flatMapIterable

class RawParameterReadingRepository @Inject constructor(
    private val rawParameterReadingDao: RawParameterReadingEntityDao,
    private val schedulerProvider: SchedulerProvider,
    private val disposable: CompositeDisposable,
) {
    /**
     * Save all sensor reading data.
     *
     * Sensor data is received with 1Hz frequency.
     * Every minute it is aggregated, the result stored in the DB for charts purposes and removed from buffer.
     * So we need to persist the data before the buffer is cleaned.
     */
    fun saveRawReadingData(type: ReadingType, values: List<Float>) {
        val data = values.map { value ->
            RawParameterReadingEntity(
                type = type,
                value = value.toDouble(),
                createdAt = Calendar.getInstance().timeInMillis,
            )
        }

        rawParameterReadingDao
            .insert(*data.toTypedArray())
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                Timber.d("Persist ${values.size} entries of ${type.key} raw data")
            }
            .addTo(disposable)
    }

    /**
     * Return data prepared for CSV export.
     * Each DB entry converted into a list of its values as strings.
     */
    fun getRawReadingCsvData(startAt: Long, endAt: Long) =
        rawParameterReadingDao
            .findAllByTypeBetweenTimestamps(startAt, endAt)
            .toObservable()
            .flatMapIterable { entity -> entity }
            .map {
                listOf(it.id, it.type, it.value, it.createdAt)
                    .map { it.toString() }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .toList()
}