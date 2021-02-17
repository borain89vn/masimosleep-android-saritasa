package com.masimo.android.airlib

internal enum class StructureVersion {
    V1,
    V2,
    UNKNOWN;
}

internal fun structureVersionFromByte (byte: Byte):StructureVersion = when (byte) {
    0x01.toByte() -> StructureVersion.V1
    0x02.toByte() -> StructureVersion.V2
    else -> StructureVersion.UNKNOWN
}