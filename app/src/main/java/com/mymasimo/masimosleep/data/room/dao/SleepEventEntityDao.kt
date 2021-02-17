package com.mymasimo.masimosleep.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mymasimo.masimosleep.data.room.entity.SleepEventContract as Contract
import com.mymasimo.masimosleep.data.room.entity.SleepEventEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface SleepEventEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sleepEvent: SleepEventEntity): Completable

    @Query("SELECT * FROM ${Contract.TABLE_NAME} " +
            "WHERE ${Contract.COLUMN_START_TIME} > :startAt " +
            "ORDER BY ${Contract.COLUMN_START_TIME} ASC")
    fun findAllAfterTimeUpdates(startAt: Long): Observable<List<SleepEventEntity>>

    @Query("SELECT * FROM ${Contract.TABLE_NAME} " +
            "WHERE ${Contract.COLUMN_START_TIME} >= :startAt " +
            "AND ${Contract.COLUMN_START_TIME} <= :endAt")
    fun findAllBetweenTimestamps(startAt: Long, endAt: Long): Single<List<SleepEventEntity>>

    @Query("SELECT COUNT(*) FROM ${Contract.TABLE_NAME} " +
            "WHERE ${Contract.COLUMN_START_TIME} >= :startAt " +
            "AND ${Contract.COLUMN_START_TIME} <= :endAt")
    fun countAllBetweenTimestamps(startAt: Long, endAt: Long): Single<Int>
}
