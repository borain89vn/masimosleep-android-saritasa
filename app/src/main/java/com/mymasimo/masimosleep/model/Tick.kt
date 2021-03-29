package com.mymasimo.masimosleep.model

import com.masimo.sleepscore.sleepscorelib.model.Parameter

data class Tick(
    val oxygenLevel: Parameter,
    val pulseRate: Parameter,
    val respirationRate: Parameter,
)
