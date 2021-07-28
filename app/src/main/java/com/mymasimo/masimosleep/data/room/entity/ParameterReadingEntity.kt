package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingContract.COLUMN_CREATED_AT
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingContract.COLUMN_DATA_POINT_COUNT
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingContract.COLUMN_TYPE
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingContract.COLUMN_VALUE
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingContract.TABLE_NAME

/**
 * Stores aggregated parameter reading. Aggregation performed once per minute.
 */
@Entity(tableName = TABLE_NAME)
data class ParameterReadingEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_TYPE) val type: ReadingType,
    @ColumnInfo(name = COLUMN_VALUE) val value: Double,
    @ColumnInfo(name = COLUMN_DATA_POINT_COUNT) val dataPointCount: Int,
    @ColumnInfo(name = COLUMN_CREATED_AT) val createdAt: Long
)

object ParameterReadingContract {
    const val TABLE_NAME = "parameter_readings"

    const val COLUMN_ID = "id"
    const val COLUMN_TYPE = "type"
    const val COLUMN_VALUE = "value"
    const val COLUMN_DATA_POINT_COUNT = "data_point_count"
    const val COLUMN_CREATED_AT = "created_at"
}
