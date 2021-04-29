package com.mymasimo.masimosleep.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mymasimo.masimosleep.data.room.entity.Module
import com.mymasimo.masimosleep.data.room.entity.ModuleContract
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(module: Module): Long

    @Query("DELETE FROM ${ModuleContract.TABLE_NAME} WHERE ${ModuleContract.ID}=:id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM ${ModuleContract.TABLE_NAME} WHERE ${ModuleContract.IS_CURRENT} = 1 LIMIT 1")
    fun getCurrentModule(): Flow<Module>

    @Query("SELECT * FROM ${ModuleContract.TABLE_NAME} ORDER BY ${ModuleContract.ID} DESC LIMIT 1")
    suspend fun getNextSensor(): Module
}
