package com.mymasimo.masimosleep.data.room.entity

import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.masimo.android.airlib.*
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
}

@Entity(tableName = ModuleContract.TABLE_NAME)
data class Module(@ColumnInfo(name = ModuleContract.MODULE_TYPE)
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
                  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ModuleContract.ID)
                  var id: Long? = null) {

    @get:Ignore
    val scanFilter: BLEScanFilterConfig
        get() = bleScanFilterConfig(type, variant, serialNumber)

    override fun toString(): String = "$manufacturerName [$type|$variant] ($serialNumber)"
}

fun bleScanFilterConfig(
    type: ProductType = ProductType.AIR_SPO2,
    variant: ProductVariant = ProductVariant.AIR_SPO2_P05,
    serialNumber: String? = null
): BLEScanFilterConfig {
    return BLEScanFilterConfig().apply {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(AIR_SERVICE_UUID))
            .setManufacturerData(MASIMO_BLUETOOTH_SIG_MANUFACTURER_ID, null)
            .build()
        serialNumber?.let {
            serialNumberFilterSet.add(SerialNumber(it))
        }

        scanFilters.add(filter)
        productTypeFilterSet.add(type)
        productVariantFilterSet.add(variant)
    }
}