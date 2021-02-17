package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.ProgramContract.COLUMN_END_DATE
import com.mymasimo.masimosleep.data.room.entity.ProgramContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.ProgramContract.COLUMN_OUTCOME
import com.mymasimo.masimosleep.data.room.entity.ProgramContract.COLUMN_SCORE
import com.mymasimo.masimosleep.data.room.entity.ProgramContract.COLUMN_START_DATE
import com.mymasimo.masimosleep.data.room.entity.ProgramContract.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class ProgramEntity(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
        @ColumnInfo(name = COLUMN_START_DATE) val startDate: Long,
        @ColumnInfo(name = COLUMN_END_DATE) var endDate: Long? = null,
        @ColumnInfo(name = COLUMN_SCORE, defaultValue = "0.0") var score: Double = 0.0,
        @ColumnInfo(name = COLUMN_OUTCOME, defaultValue = "0.0") var outcome: Double = 0.0
)

@Entity(
        foreignKeys = [
            ForeignKey(
                    entity = ProgramEntity::class,
                    parentColumns = [COLUMN_ID],
                    childColumns = [SessionContract.COLUMN_PROGRAM_ID],
                    onDelete = ForeignKey.CASCADE
            )
        ]
)

object ProgramContract {
    const val TABLE_NAME = "programs"

    const val COLUMN_ID = "id"
    const val COLUMN_START_DATE = "startDate"
    const val COLUMN_END_DATE = "endDate"
    const val COLUMN_SCORE = "score"
    const val COLUMN_OUTCOME = "outcome"
}
