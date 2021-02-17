package com.masimo.android.airlib

enum class ProductType(byteValue: Byte) {
    AIR_SPO2(0x04.toByte()),
    OTHER(0);
}

internal fun productTypeFromByte(byte: Byte): ProductType = if (byte == 4.toByte()) ProductType.AIR_SPO2 else ProductType.OTHER