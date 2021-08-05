package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.RawParameterReadingEntityDao
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RawParameterReadingRepository @Inject constructor(
    private val rawParameterReadingDao: RawParameterReadingEntityDao,
    private val schedulerProvider: SchedulerProvider,
    private val disposable: CompositeDisposable,
) {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

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
    fun getRawReadingCsvData(startAt: Long, endAt: Long, nightNumber: Int) =
        rawParameterReadingDao
            .findAllByTypeBetweenTimestamps(startAt, endAt)
            .toObservable()
            .flatMapIterable { list ->
                list.groupBy { dateTimeFormat.format(Date(it.createdAt)) }
                    .map { readingsByDate ->
                        var prValue: Double? = null
                        var spo2Value: Double? = null
                        var rrpValue: Double? = null
                        readingsByDate.value.forEach { entity ->
                            when (entity.type) {
                                ReadingType.PR -> prValue = entity.value
                                ReadingType.SP02 -> spo2Value = entity.value
                                ReadingType.RRP -> rrpValue = entity.value
                                else -> {}
                            }
                        }
                        listOf(
                            readingsByDate.key, // created_at
                            nightNumber, // night
                            spo2Value ?: "", // spo2_value
                            prValue ?: "", // pr_value
                            rrpValue ?: "", // rrp_value
                        )
                            .map { it.toString() }
                    }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .toList()
}