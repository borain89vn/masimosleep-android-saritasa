package com.mymasimo.masimosleep.data.room.entity

enum class SurveyQuestion(val key: String) {
    CAFFEINE_("caffeine"),
    SNORING("snoring"),
    ALCOHOL("alcohol"),
    EXERCISE("exercise"),
    SLEEP_DRUG("sleep_drug");

    companion object {
        private val map = values().associateBy { it.key }
        fun fromKey(key: String): SurveyQuestion = map[key]
            ?: throw IllegalArgumentException("Invalid key $key")
    }
}
