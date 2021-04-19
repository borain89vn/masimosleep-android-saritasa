package com.mymasimo.masimosleep.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.masimo.common.model.universal.ParameterID
import com.masimo.sleepscore.sleepscorelib.model.Parameter
import com.mymasimo.masimosleep.base.dispatchers.CoroutineDispatchers
import com.mymasimo.masimosleep.data.asFlow
import com.mymasimo.masimosleep.data.await
import com.mymasimo.masimosleep.data.room.entity.Module
import com.mymasimo.masimosleep.model.Tick
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorFirestoreRepository @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
) {
    val db = Firebase.firestore
    private fun sensorDocument(id: String) = db.collection("users").document(id)

    @ExperimentalCoroutinesApi
    fun getTicks(sensor: Module): Flow<Tick> = sensorDocument(sensor.address.toDocumentId()).asFlow().map { it.toTick() }

    suspend fun insertSensor(sensor: Module) = withContext(dispatchers.io()) {
        val name = sensor.address
        val data = mapOf(
            "username" to name,
            OXYGEN_LEVEL to 98F,
            PULSE_RATE to 88F,
            RESPIRATION_RATE to 33F,
        )

        sensorDocument(name.toDocumentId()).set(data).await()
    }
}

fun DocumentSnapshot.toTick(): Tick {
    val oxygenLevel = Parameter(ParameterID.FUNC_SPO2, value = this[OXYGEN_LEVEL, Float::class.java] ?: 0F, exceptionBitmask = 0x00)
    val pulseRate = Parameter(ParameterID.PR, value = this[PULSE_RATE, Float::class.java] ?: 0F, exceptionBitmask = 0x00)
    val respirationRate = Parameter(ParameterID.RRP, value = this[RESPIRATION_RATE, Float::class.java] ?: 0F, exceptionBitmask = 0x00)
    return Tick(oxygenLevel, pulseRate, respirationRate)
}

private const val OXYGEN_LEVEL = "spO2"
private const val PULSE_RATE = "pr"
private const val RESPIRATION_RATE = "rrp"

//private fun String.toDocumentId(): String = Base64.encodeToString(encodeToByteArray(), Base64.DEFAULT)
private fun String.toDocumentId(): String = hashCode().toString()