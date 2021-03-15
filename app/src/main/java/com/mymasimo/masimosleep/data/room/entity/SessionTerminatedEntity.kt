package com.mymasimo.masimosleep.data.room.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract.COLUMN_CAUSE
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract.COLUMN_HANDLED
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract.COLUMN_RECORDED
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract.COLUMN_SESSION_ID
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract.COLUMN_SESSION_NIGHT
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedContract.TABLE_NAME
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = TABLE_NAME)
data class SessionTerminatedEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_SESSION_ID) val sessionId: Long?,
    @ColumnInfo(name = COLUMN_SESSION_NIGHT) val night: Int?,
    @ColumnInfo(name = COLUMN_CAUSE) val cause: SessionTerminatedCause?,
    @ColumnInfo(name = COLUMN_HANDLED) val handled: Boolean,
    @ColumnInfo(name = COLUMN_RECORDED) val recorded: Boolean,
) : Parcelable

object SessionTerminatedContract {
    const val TABLE_NAME = "session_terminated"

    const val COLUMN_ID = "id"
    const val COLUMN_SESSION_ID = "sessionId"
    const val COLUMN_SESSION_NIGHT = "night"
    const val COLUMN_CAUSE = "cause"
    const val COLUMN_HANDLED = "handled"
    const val COLUMN_RECORDED = "recorded"
}
