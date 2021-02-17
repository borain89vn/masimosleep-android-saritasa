package com.mymasimo.masimosleep.ui.session.view_vitals

enum class ChartIntervalType {
    ALL,
    HOUR,
    MINUTE,
    INT;
}

fun chartIntervalTypeFromInt(status: Int) = ChartIntervalType.values()[status]