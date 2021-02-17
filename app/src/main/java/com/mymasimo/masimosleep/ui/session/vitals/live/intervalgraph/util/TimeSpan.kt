package com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.util

data class TimeSpan(
    val minutes: Int
)

fun TimeSpan.toMillis(): Long {
    return minutes * 60 * 1000L
}


