package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.ScoreContract.COLUMN_CREATED_AT
import com.mymasimo.masimosleep.data.room.entity.ScoreContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.ScoreContract.COLUMN_SESSION_ID
import com.mymasimo.masimosleep.data.room.entity.ScoreContract.COLUMN_TYPE
import com.mymasimo.masimosleep.data.room.entity.ScoreContract.COLUMN_VALUE
import com.mymasimo.masimosleep.data.room.entity.ScoreContract.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_SESSION_ID) val sessionId: Long,
    @ColumnInfo(name = COLUMN_VALUE) val value: Double,
    @ColumnInfo(name = COLUMN_TYPE) val type: ScoreType,
    @ColumnInfo(name = COLUMN_CREATED_AT) val createdAt: Long
)

object ScoreContract {
    const val TABLE_NAME = "scores"

    const val COLUMN_ID = "id"
    const val COLUMN_SESSION_ID = "sessionId"
    const val COLUMN_VALUE = "value"
    const val COLUMN_TYPE = "type"
    const val COLUMN_CREATED_AT = "created_at"
}