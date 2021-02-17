package com.mymasimo.masimosleep.data.room.entity

enum class SurveyAnswer(val key: String) {
    YES("yes"), NO("no"), NO_ANSWER("no_answer");

    companion object {
        val map = values().associateBy { it.key }
        fun fromKey(key: String): SurveyAnswer = map[key]
            ?: throw IllegalArgumentException("Invalid key $key")
    }
}
