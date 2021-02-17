package com.mymasimo.masimosleep.ui.program_history.util

interface IProgram {
    val sessionCount: Int
    val id: Long
    val score: Double
}

sealed class Program : IProgram {
    data class Current(
            override val id: Long,
            val startAt: Long,
            override val score: Double,
            override val sessionCount: Int
    ) : Program()

    data class Past(
            override val id: Long,
            val startAt: Long,
            val endAt: Long,
            override val score: Double,
            override val sessionCount: Int = 0
    ) : Program()
}
