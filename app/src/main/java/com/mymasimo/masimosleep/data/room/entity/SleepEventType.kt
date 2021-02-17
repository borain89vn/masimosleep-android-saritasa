package com.mymasimo.masimosleep.data.room.entity

import com.masimo.sleepscore.sleepscorelib.model.SleepEventType as MasimoSleepEventType

enum class SleepEventType(val key: String) {
    MILD("mild"), SEVERE("severe");

    companion object {
        private val map = values().associateBy { it.key }

        fun fromKey(key: String): SleepEventType {
            return map[key] ?: throw IllegalArgumentException("Invalid key: $key")
        }

        fun fromMasimoType(
            type: MasimoSleepEventType
        ): SleepEventType {
            return when (type) {
                MasimoSleepEventType.NONE -> throw IllegalArgumentException("Unsupported NONE type")
                MasimoSleepEventType.MILD -> MILD
                MasimoSleepEventType.SEVERE -> SEVERE
            }
        }
    }
}
