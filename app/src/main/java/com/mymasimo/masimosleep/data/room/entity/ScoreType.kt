package com.mymasimo.masimosleep.data.room.entity

import com.masimo.sleepscore.sleepscorelib.model.SleepSessionScore
import java.lang.IllegalArgumentException

enum class ScoreType(val key: String) {
    LIVE("live"), SESSION("session"), PROGRAM("program");

    companion object {
        private val map = values().associateBy { it.key }

        fun fromKey(key: String): ScoreType {
            return map[key] ?: throw IllegalArgumentException("Invalid key: $key")
        }

        fun fromSleepSessionScoreType(type: SleepSessionScore.SleepSessionScoreType): ScoreType {
            return when (type) {
                SleepSessionScore.SleepSessionScoreType.LIVE -> LIVE
                SleepSessionScore.SleepSessionScoreType.SESSION -> SESSION
                SleepSessionScore.SleepSessionScoreType.PROGRAM -> PROGRAM
            }
        }
    }
}