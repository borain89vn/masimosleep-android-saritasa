package com.mymasimo.masimosleep.model

import com.mymasimo.masimosleep.data.room.entity.SleepEventType

data class FireStoreSleepEvent(val startAt: Long, val endAt: Long, val type: SleepEventType) {

    override fun toString(): String {
        return "startAt = ${startAt}, endAt = ${endAt}, sleepEventType = ${type.name}"
    }
}