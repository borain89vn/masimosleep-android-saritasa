package com.mymasimo.masimosleep.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingContract as Contract


@Dao
interface ParameterReadingEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg parameterReadings: ParameterReadingEntity): Completable

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_TYPE} = 'spO2' " +
                "ORDER BY ${Contract.COLUMN_CREATED_AT} DESC " +
                "LIMIT 1"
    )
    fun latestSPO2Reading(): Maybe<ParameterReadingEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_TYPE} = 'pr' " +
                "ORDER BY ${Contract.COLUMN_CREATED_AT} DESC " +
                "LIMIT 1"
    )
    fun latestPRReading(): Maybe<ParameterReadingEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_TYPE} = 'rrp' " +
                "ORDER BY ${Contract.COLUMN_CREATED_AT} DESC " +
                "LIMIT 1"
    )
    fun latestRPRReading(): Maybe<ParameterReadingEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_TYPE} = :type AND ${Contract.COLUMN_CREATED_AT} >= :startAt " +
                "ORDER BY ${Contract.COLUMN_CREATED_AT} ASC"
    )
    fun findAllByTypeAfterTimestampUpdating(
        type: ReadingType,
        startAt: Long
    ): Observable<List<ParameterReadingEntity>>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_TYPE} = :type " +
                "AND ${Contract.COLUMN_CREATED_AT} >= :startAt " +
                "AND ${Contract.COLUMN_CREATED_AT} < :endAt " +
                "ORDER BY ${Contract.COLUMN_CREATED_AT} ASC"
    )
    fun findAllByTypeBetweenTimestamps(
        type: ReadingType,
        startAt: Long,
        endAt: Long
    ): Single<List<ParameterReadingEntity>>
}
