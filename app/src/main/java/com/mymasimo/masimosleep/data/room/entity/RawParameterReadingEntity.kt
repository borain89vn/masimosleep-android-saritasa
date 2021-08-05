package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingContract.COLUMN_CREATED_AT
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingContract.COLUMN_ID
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingContract.COLUMN_TYPE
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingContract.COLUMN_VALUE
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingContract.TABLE_NAME
import java.util.*

/**
 * Stores raw parameter reading.
 * Same to `ParameterReadingEntity`, but this table stores all the data collected from sensors, not aggregated data.
 *
 * Should not be used for charts.
 */
@Entity(tableName = TABLE_NAME)
data class RawParameterReadingEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long? = null,
    @ColumnInfo(name = COLUMN_TYPE) val type: ReadingType,
    @ColumnInfo(name = COLUMN_VALUE) val value: Double,
    @ColumnInfo(name = COLUMN_CREATED_AT) val createdAt: Long = Calendar.getInstance().timeInMillis,
)

object RawParameterReadingContract {
    const val TABLE_NAME = "raw_parameter_readings"

    const val COLUMN_ID = "id"
    const val COLUMN_TYPE = "type"
    const val COLUMN_VALUE = "value"
    const val COLUMN_CREATED_AT = "created_at"
    const val COLUMN_SPO2 = "spo2_value"
    const val COLUMN_PR = "pr_value"
    const val COLUMN_RRP = "rrp_value"
}
