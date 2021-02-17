package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.SessionNoteContract.COLUMN_CREATED_AT
import com.mymasimo.masimosleep.data.room.entity.SessionNoteContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.SessionNoteContract.COLUMN_NOTE
import com.mymasimo.masimosleep.data.room.entity.SessionNoteContract.COLUMN_SESSION_ID
import com.mymasimo.masimosleep.data.room.entity.SessionNoteContract.TABLE_NAME
import java.util.*

@Entity(tableName = TABLE_NAME)
data class SessionNoteEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_SESSION_ID) val sessionId: Long,
    @ColumnInfo(name = COLUMN_NOTE) val note: String,
    @ColumnInfo(name = COLUMN_CREATED_AT) val createdAt: Long = Calendar.getInstance().timeInMillis
)

object SessionNoteContract {
    const val TABLE_NAME = "session_notes"

    const val COLUMN_ID = "id"
    const val COLUMN_SESSION_ID = "sessionId"
    const val COLUMN_NOTE = "note"
    const val COLUMN_CREATED_AT = "created_at"
}
