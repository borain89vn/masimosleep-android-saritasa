package com.mymasimo.masimosleep.ui.program_report.nightly_scores

enum class NightScoreButtonState {
    PAST,
    FUTURE;
}

fun nightScoreButtonStateFromInt(status: Int) = NightScoreButtonState.values()[status]