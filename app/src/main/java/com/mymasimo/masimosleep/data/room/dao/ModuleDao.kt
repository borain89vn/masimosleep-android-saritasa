package com.mymasimo.masimosleep.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mymasimo.masimosleep.data.room.entity.Module
import com.mymasimo.masimosleep.data.room.entity.ModuleContract
import io.reactivex.Maybe
import io.reactivex.Observable

@Dao
interface ModuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(module: Module): Long

    @Query("DELETE FROM ${ModuleContract.TABLE_NAME} WHERE ${ModuleContract.ID}=:id")
    fun delete(id: Long): Int

    @Query("SELECT * FROM ${ModuleContract.TABLE_NAME} WHERE ${ModuleContract.ID}=:moduleId LIMIT 1")
    fun getModule(moduleId: Long): Observable<Module>

    @Query("SELECT ${ModuleContract.ID} FROM ${ModuleContract.TABLE_NAME} ORDER BY ${ModuleContract.ID} DESC LIMIT 1")
    fun getNextSelectedId(): Maybe<Long>
}
