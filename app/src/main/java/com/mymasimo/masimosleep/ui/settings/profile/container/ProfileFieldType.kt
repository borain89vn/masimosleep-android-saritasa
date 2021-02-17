package com.mymasimo.masimosleep.ui.settings.profile.container

enum class ProfileFieldType {
    NAME,
    GENDER,
    BIRTHDATE,
    CONDITIONS,
    BEDTIME,
    REMINDER
}

fun profileFieldTypeFromInt(status: Int) = ProfileFieldType.values()[status]
