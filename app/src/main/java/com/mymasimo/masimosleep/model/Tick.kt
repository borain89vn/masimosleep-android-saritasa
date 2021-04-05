package com.mymasimo.masimosleep.model

import com.masimo.sleepscore.sleepscorelib.model.Parameter

data class Tick(
    val oxygenLevel: Parameter,
    val pulseRate: Parameter,
    val respirationRate: Parameter,
) {
    override fun toString(): String {
        return "oxygenLevel = ${oxygenLevel.value}, pulseRate = ${pulseRate.value}, respirationRate = ${respirationRate.value}"
    }
}
