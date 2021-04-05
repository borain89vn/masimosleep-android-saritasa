package com.mymasimo.masimosleep.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mymasimo.masimosleep.data.room.entity.SessionContract
import com.mymasimo.masimosleep.data.room.entity.SessionNoteContract
import com.mymasimo.masimosleep.data.room.entity.SessionNoteEntity
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface SessionNoteEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sessionNote: SessionNoteEntity): Completable

    @Query(
        "SELECT * FROM ${SessionNoteContract.TABLE_NAME} " +
                "INNER JOIN ${SessionContract.TABLE_NAME} " +
                "ON (${SessionNoteContract.TABLE_NAME}.${SessionNoteContract.COLUMN_SESSION_ID}=" +
                "${SessionContract.TABLE_NAME}.${SessionContract.COLUMN_ID}) " +
                "WHERE ${SessionContract.TABLE_NAME}.${SessionContract.COLUMN_ID} = :sessionId " +
                "ORDER BY ${SessionNoteContract.COLUMN_CREATED_AT} ASC"
    )
    fun findAllBySessionId(sessionId: Long): Observable<List<SessionNoteEntity>>
}