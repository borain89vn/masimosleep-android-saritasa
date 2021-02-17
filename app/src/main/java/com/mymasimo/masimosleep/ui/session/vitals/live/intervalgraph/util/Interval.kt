package com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.util

data class Interval(
    val minutes: Int
)

fun Interval.toMillis(): Long {
    return minutes * 60 * 1000L
}


