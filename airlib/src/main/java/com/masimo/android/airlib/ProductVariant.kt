package com.masimo.android.airlib

enum class ProductVariant {
    AIR_SPO2_P05,
    OTHER;
}

internal fun ProductType.variantFromByte(byte: Byte): ProductVariant = if (this == ProductType.AIR_SPO2) getAirSpO2Variant(byte) else ProductVariant.OTHER

private fun getAirSpO2Variant(byte: Byte): ProductVariant = if (byte == 5.toByte()) ProductVariant.AIR_SPO2_P05 else ProductVariant.OTHER
