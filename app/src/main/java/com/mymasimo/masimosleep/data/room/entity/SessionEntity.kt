package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.SessionContract.COLUMN_END_AT
import com.mymasimo.masimosleep.data.room.entity.SessionContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.SessionContract.COLUMN_NIGHT_NUMBER
import com.mymasimo.masimosleep.data.room.entity.SessionContract.COLUMN_PROGRAM_ID
import com.mymasimo.masimosleep.data.room.entity.SessionContract.COLUMN_START_AT
import com.mymasimo.masimosleep.data.room.entity.SessionContract.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_PROGRAM_ID) val programId: Long,
    @ColumnInfo(name = COLUMN_NIGHT_NUMBER) val nightNumber: Int,
    @ColumnInfo(name = COLUMN_START_AT) val startAt: Long,
    @ColumnInfo(name = COLUMN_END_AT) var endAt: Long? = null
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = [COLUMN_ID],
            childColumns = [SessionNoteContract.COLUMN_SESSION_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SurveyQuestionEntity::class,
            parentColumns = [COLUMN_ID],
            childColumns = [SurveyContract.COLUMN_SESSION_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScoreEntity::class,
            parentColumns = [COLUMN_ID],
            childColumns = [ScoreContract.COLUMN_SESSION_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

object SessionContract {
    const val TABLE_NAME = "sessions"

    const val COLUMN_ID = "id"
    const val COLUMN_PROGRAM_ID = "program_id"
    const val COLUMN_NIGHT_NUMBER = "night_number"
    const val COLUMN_START_AT = "start_at"
    const val COLUMN_END_AT = "end_at"
}
