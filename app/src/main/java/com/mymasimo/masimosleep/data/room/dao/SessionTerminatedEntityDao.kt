package com.mymasimo.masimosleep.data.room.dao

import androidx.room.*
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface SessionTerminatedEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sessionTerminatedEntity: SessionTerminatedEntity): Completable

    @Update
    fun update(sessionTerminatedEntity: SessionTerminatedEntity): Completable

    @Delete
    fun delete(sessionTerminatedEntity: SessionTerminatedEntity): Completable

    @Query("SELECT * FROM ${SessionTerminatedContract.TABLE_NAME} " +
                   "WHERE ${SessionTerminatedContract.COLUMN_HANDLED} = 0 " +
                   "ORDER BY ${SessionTerminatedContract.COLUMN_ID} DESC LIMIT 1")
    fun findLatestTerminatedModelNotHandled(): Single<SessionTerminatedEntity>
}