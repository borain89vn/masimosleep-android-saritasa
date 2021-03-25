package com.mymasimo.masimosleep.data

import kotlinx.coroutines.flow.Flow

interface Sensor {

    val oxygenLevel: Flow<Int>
    val pulseRate: Flow<Int>
    val respirationRate: Flow<Int>
}