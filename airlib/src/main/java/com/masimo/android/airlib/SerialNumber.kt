package com.masimo.android.airlib

import java.nio.ByteBuffer

data class SerialNumber(val bytes: ByteArray) {

    constructor(value: String) : this(value.toByteArray())
    constructor(value: Long) : this(ByteBuffer.allocate(java.lang.Long.SIZE / java.lang.Byte.SIZE).putLong(value).array())

    override fun toString(): String {
        return String(bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerialNumber

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}
