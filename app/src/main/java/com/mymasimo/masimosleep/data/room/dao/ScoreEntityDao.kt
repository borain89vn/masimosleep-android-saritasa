package com.mymasimo.masimosleep.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mymasimo.masimosleep.data.room.entity.ScoreContract as Contract
import com.mymasimo.masimosleep.data.room.entity.ScoreEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ScoreEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(score: ScoreEntity): Completable

    @Query("SELECT * FROM ${Contract.TABLE_NAME} " +
            "WHERE ${Contract.COLUMN_TYPE} = 'live' " +
            "AND ${Contract.COLUMN_CREATED_AT} >= :startAt " +
            "ORDER BY ${Contract.COLUMN_CREATED_AT} DESC " +
            "LIMIT 1")
    fun recentLiveScoreUpdates(startAt: Long): Flowable<ScoreEntity>

    @Query("SELECT * FROM ${Contract.TABLE_NAME} " +
            "WHERE ${Contract.COLUMN_TYPE} = 'live' " +
            "AND ${Contract.COLUMN_CREATED_AT} >= :sessionStartAt " +
            "ORDER BY ${Contract.COLUMN_CREATED_AT} ASC")
    fun findAllLiveScoresFrom(sessionStartAt: Long): Single<List<ScoreEntity>>

    @Query("SELECT * FROM ${Contract.TABLE_NAME} " +
            "WHERE ${Contract.COLUMN_TYPE} = 'live' " +
            "AND ${Contract.COLUMN_CREATED_AT} >= :startAt " +
            "AND ${Contract.COLUMN_CREATED_AT} <= :endAt")
    fun findLiveScoresBetweenTimestamps(startAt: Long, endAt: Long): Single<List<ScoreEntity>>

    @Query("SELECT * FROM ${Contract.TABLE_NAME} " +
            "WHERE ${Contract.COLUMN_TYPE} = 'session' " +
            "AND ${Contract.COLUMN_SESSION_ID} = :sessionId " +
            "LIMIT 1")
    fun findSessionScoreBySessionId(sessionId: Long): Single<ScoreEntity>
}
