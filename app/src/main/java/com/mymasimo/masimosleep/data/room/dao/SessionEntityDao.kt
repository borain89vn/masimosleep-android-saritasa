package com.mymasimo.masimosleep.data.room.dao

import androidx.room.*
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import io.reactivex.Completable
import io.reactivex.Single
import com.mymasimo.masimosleep.data.room.entity.SessionContract as Contract

@Dao
interface SessionEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: SessionEntity): Completable

    @Update
    fun update(session: SessionEntity): Completable

    @Delete
    fun delete(session: SessionEntity): Completable

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_END_AT} is null " +
                "ORDER BY ${Contract.COLUMN_START_AT} DESC " +
                "LIMIT 1"
    )
    fun findSessionInProgress(): Single<SessionEntity>

    @Query(
        "SELECT ${Contract.COLUMN_ID} FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_END_AT} is null " +
                "ORDER BY ${Contract.COLUMN_START_AT} DESC " +
                "LIMIT 1"
    )
    fun findSessionInProgressId(): Single<Long>

    @Query("SELECT * FROM ${Contract.TABLE_NAME} WHERE ${Contract.COLUMN_ID} = :sessionId")
    fun findSessionById(sessionId: Long): Single<SessionEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_PROGRAM_ID} = :programId " +
                "ORDER BY ${Contract.COLUMN_END_AT} DESC LIMIT 1"
    )
    fun findLastEndedSessionForProgram(programId: Long): Single<SessionEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_PROGRAM_ID} = :programId " +
                "ORDER BY ${Contract.COLUMN_START_AT} ASC"
    )
    fun findAllByProgramIdAsc(programId: Long): Single<List<SessionEntity>>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_PROGRAM_ID} = :programId"
    )
    fun findAllByProgramId(programId: Long): Single<List<SessionEntity>>

    @Query(
        "SELECT COUNT(*) FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_PROGRAM_ID} = :programId"
    )
    fun countAllByProgramId(programId: Long): Single<Int>
}
