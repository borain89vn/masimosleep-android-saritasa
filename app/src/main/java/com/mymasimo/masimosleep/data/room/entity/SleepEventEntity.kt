package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.SleepEventContract.COLUMN_END_TIME
import com.mymasimo.masimosleep.data.room.entity.SleepEventContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.SleepEventContract.COLUMN_SPO2
import com.mymasimo.masimosleep.data.room.entity.SleepEventContract.COLUMN_START_TIME
import com.mymasimo.masimosleep.data.room.entity.SleepEventContract.COLUMN_TYPE
import com.mymasimo.masimosleep.data.room.entity.SleepEventContract.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class SleepEventEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_SPO2) val spO2: Double,
    @ColumnInfo(name = COLUMN_TYPE) val type: SleepEventType,
    @ColumnInfo(name = COLUMN_START_TIME) val startTime: Long,
    @ColumnInfo(name = COLUMN_END_TIME) val endTime: Long
)

object SleepEventContract {
    const val TABLE_NAME = "sleep_events"

    const val COLUMN_ID = "id"
    const val COLUMN_TYPE = "type"
    const val COLUMN_START_TIME = "start_time"
    const val COLUMN_END_TIME = "end_time"
    const val COLUMN_SPO2 = "spO2"
}
