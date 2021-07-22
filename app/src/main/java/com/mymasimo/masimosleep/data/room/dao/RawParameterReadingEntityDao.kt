package com.mymasimo.masimosleep.data.room.dao

import androidx.room.*
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import io.reactivex.Completable
import io.reactivex.Single
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingContract as Contract


@Dao
interface RawParameterReadingEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg parameters: RawParameterReadingEntity): Completable

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_TYPE} = :type " +
                "AND ${Contract.COLUMN_CREATED_AT} >= :startAt " +
                "AND ${Contract.COLUMN_CREATED_AT} < :endAt " +
                "ORDER BY ${Contract.COLUMN_ID} ASC, ${Contract.COLUMN_CREATED_AT} ASC"
    )
    fun findAllByTypeBetweenTimestamps(
        type: ReadingType,
        startAt: Long,
        endAt: Long
    ): Single<List<RawParameterReadingEntity>>
}
