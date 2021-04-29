package com.mymasimo.masimosleep.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.masimo.android.airlib.ProductType
import com.masimo.android.airlib.ProductVariant
import com.masimo.common.model.universal.ParameterID
import java.util.*

object ModuleContract {
    const val TABLE_NAME = "modules"

    const val ID = "id"
    const val MODULE_TYPE = "module_type"
    const val MODEL_TYPE = "model_type"
    const val MANUFACTURER_NAME = "manufacturer_name"
    const val FIRMWARE_VERSION = "firmware_version"
    const val SERIAL_NUMBER = "serial_number"
    const val ADDRESS = "address"
    const val SUPPORTED_PARAMETERS = "supported_parameters"
    const val IS_CURRENT = "is_current"
}

@Entity(tableName = ModuleContract.TABLE_NAME)
data class Module(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ModuleContract.ID)
    var id: Long? = null,

    @ColumnInfo(name = ModuleContract.MODULE_TYPE)
    var type: ProductType,

    @ColumnInfo(name = ModuleContract.MODEL_TYPE)
    var variant: ProductVariant,

    @ColumnInfo(name = ModuleContract.MANUFACTURER_NAME)
    var manufacturerName: String,

    @ColumnInfo(name = ModuleContract.FIRMWARE_VERSION)
    var firmwareVersion: String,

    @ColumnInfo(name = ModuleContract.SERIAL_NUMBER)
    var serialNumber: String,

    @ColumnInfo(name = ModuleContract.ADDRESS)
    var address: String,

    @ColumnInfo(name = ModuleContract.SUPPORTED_PARAMETERS)
    var supportedParameters: EnumSet<ParameterID>,

    @ColumnInfo(name = ModuleContract.IS_CURRENT)
    val isCurrent: Boolean,
) {
    override fun toString(): String = "$manufacturerName [$type|$variant] ($serialNumber)"
}