package com.mymasimo.masimosleep.data.room.dao

import androidx.room.*
import com.mymasimo.masimosleep.data.room.entity.ProgramEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import com.mymasimo.masimosleep.data.room.entity.ProgramContract as Contract

@Dao
interface ProgramEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(program: ProgramEntity)

    @Update
    fun update(program: ProgramEntity): Completable

    @Query("SELECT * FROM ${Contract.TABLE_NAME} WHERE ${Contract.COLUMN_ID} = :programId LIMIT 1")
    fun findById(programId: Long): Single<ProgramEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_END_DATE} IS NULL " +
                "ORDER BY ${Contract.COLUMN_START_DATE} DESC " +
                "LIMIT 1"
    )
    fun findCurrentProgramIfExists(): Maybe<ProgramEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "WHERE ${Contract.COLUMN_END_DATE} IS NULL " +
                "ORDER BY ${Contract.COLUMN_START_DATE} DESC " +
                "LIMIT 1"
    )
    fun findCurrentProgram(): Single<ProgramEntity>

    @Query(
        "SELECT * FROM ${Contract.TABLE_NAME} " +
                "ORDER BY ${Contract.COLUMN_START_DATE} DESC " +
                "LIMIT 1"
    )
    fun findLatestProgram(): Single<ProgramEntity>

    @Query("SELECT * FROM ${Contract.TABLE_NAME} ORDER BY ${Contract.COLUMN_START_DATE} DESC")
    fun findAllDesc(): Single<List<ProgramEntity>>

    @Query("SELECT * FROM ${Contract.TABLE_NAME} WHERE ${Contract.COLUMN_ID} = :programId LIMIT 1")
    fun findByIdFlowable(programId: Long): Flowable<ProgramEntity>
}
