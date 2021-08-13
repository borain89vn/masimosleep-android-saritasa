package com.mymasimo.masimosleep.ui.home.night_picker

enum class NightButtonState {
    BLANK,
    PAST,
    PAST_SELECTED,
    PRESENT,
    PRESENT_SELECTED,
    FUTURE;
}

fun nightButtonStateFromInt(status: Int) = NightButtonState.values()[status]