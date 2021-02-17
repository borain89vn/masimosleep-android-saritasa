package com.mymasimo.masimosleep.ui.home.night_picker

enum class NightButtonState {
    BLANK,
    PAST,
    PAST_SELECTED,
    PRESENT,
    FUTURE;
}

fun nightButtonStateFromInt(status: Int) = NightButtonState.values()[status]