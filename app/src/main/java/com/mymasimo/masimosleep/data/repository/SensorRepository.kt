package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.BuildConfig
import com.mymasimo.masimosleep.base.dispatchers.CoroutineDispatchers
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.room.dao.ModuleDao
import com.mymasimo.masimosleep.data.room.entity.Module
import kotlinx.coroutines.flow.Flow
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
    suspend fun addSensor(sensor: Module) = withContext(dispatchers.io()) {
        if (BuildConfig.ALLOW_EMULATION) {
            sensorFirestoreRepository.insertSensor(sensor)
            MasimoSleepPreferences.emulatorUsed = true
        } else {
            MasimoSleepPreferences.emulatorUsed = false
        }

        val id = sensorDao.insert(sensor)
        sensor.id = id
        Timber.d("Inserted ${sensor.type}|${sensor.variant} at row ${sensor.id}")
        MasimoSleepPreferences.selectedModuleId = id
        ModelStore.currentModule = sensor
    }


    fun loadSensor(id: Long): Flow<Module> = sensorDao.getModule(id)

    suspend fun deleteSensor(id: Long): Int = withContext(dispatchers.io()) {
        val rowsAffected = sensorDao.delete(id)
        Timber.d("Deleted $rowsAffected modules (id=$id)")
        updateSelectedSensor(id)
        rowsAffected
    }

    private suspend fun updateSelectedSensor(deletedModuleId: Long) = withContext(dispatchers.io()) {
        // nothing to do if the selected module wasn't deleted
        if (deletedModuleId != MasimoSleepPreferences.selectedModuleId) {
            Timber.d("Selected module not deleted. Not updating selectedModuleId.")
            return@withContext
        }

        // replace the selected module id with the next
        try {
            val nextId = sensorDao.getNextSelectedId()
            Timber.d("Next module id: $nextId")
            MasimoSleepPreferences.selectedModuleId = nextId
        } catch (e: Throwable) {
            Timber.e(e, "Error getting next selected module id")
            MasimoSleepPreferences.selectedModuleId = 0L
        }
    }
}