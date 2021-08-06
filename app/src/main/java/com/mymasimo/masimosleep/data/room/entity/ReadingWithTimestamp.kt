package com.mymasimo.masimosleep.data.room.entity

/**
 * Helper class to preserve readings and their timestamps to be saved later
 *
 * @property type      type of reading
 * @property value     value of reading
 * @property timestamp timestamp of reading
 */
data class ReadingWithTimestamp(
    val type: ReadingType,
    val value: Float,
    val timestamp: Long,
)
