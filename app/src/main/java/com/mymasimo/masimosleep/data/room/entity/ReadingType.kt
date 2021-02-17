package com.mymasimo.masimosleep.data.room.entity

enum class ReadingType(val key: String) {
    SP02("spO2"), PR("pr"), RRP("rrp"), DEFAULT("default");

    companion object {
        private val map = values().associateBy { it.key }

        fun fromKey(key: String): ReadingType {
            return map[key] ?: throw IllegalArgumentException("Invalid key: $key")
        }
    }
}
