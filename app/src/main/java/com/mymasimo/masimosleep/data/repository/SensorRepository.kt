package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.BuildConfig
import com.mymasimo.masimosleep.base.dispatchers.CoroutineDispatchers
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.room.dao.ModuleDao
import com.mymasimo.masimosleep.data.room.entity.Module
import com.mymasimo.masimosleep.model.Tick
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val sensorDao: ModuleDao,
    private val sensorFirestoreRepository: SensorFirestoreRepository,
    private val dispatchers: CoroutineDispatchers,
) {
    /**
     * Saves sensor as current in app. Supports emulation.
     */
    suspend fun addSensor(sensor: Module, isEmulator: Boolean = false) = withContext(dispatchers.io()) {
        if (BuildConfig.ALLOW_EMULATION && isEmulator) {
            sensorFirestoreRepository.insertSensor(sensor)
            MasimoSleepPreferences.emulatorUsed = true
        } else {
            MasimoSleepPreferences.emulatorUsed = false
        }

        val id = sensorDao.insert(sensor)
        sensor.id = id
        Timber.d("Inserted ${sensor.type}|${sensor.variant} at row ${sensor.id}")
        MasimoSleepPreferences.selectedModuleId = id
    }

    suspend fun getSelectedSensor(): Module? = withContext(dispatchers.io()) {
        val sensorId = MasimoSleepPreferences.selectedModuleId
        loadSensor(sensorId).firstOrNull()
    }

    fun loadSensor(id: Long): Flow<Module> = sensorDao.getModule(id)

    suspend fun deleteSensor(id: Long): Int {
        val rowsAffected = sensorDao.delete(id)
        Timber.d("Deleted $rowsAffected sensors (id=$id)")
        updateSelectedSensor(id)
        return rowsAffected
    }

    @ExperimentalCoroutinesApi
    fun getTicks(sensor: Module): Flow<Tick> = if (BuildConfig.ALLOW_EMULATION) sensorFirestoreRepository.getTicks(sensor) else emptyFlow()

    private suspend fun updateSelectedSensor(deletedModuleId: Long) = withContext(dispatchers.io()) {
        // nothing to do if the selected module wasn't deleted
        if (deletedModuleId != MasimoSleepPreferences.selectedModuleId) {
            Timber.d("Selected sensor not deleted.")
            return@withContext
        }

        // replace the selected module id with the next
        try {
            val nextId = sensorDao.getNextSelectedId()
            Timber.d("Next sensor id: $nextId")
            MasimoSleepPreferences.selectedModuleId = nextId
        } catch (e: Throwable) {
            Timber.e(e, "Error getting next selected sensor id")
            MasimoSleepPreferences.selectedModuleId = 0L
        }
    }
}