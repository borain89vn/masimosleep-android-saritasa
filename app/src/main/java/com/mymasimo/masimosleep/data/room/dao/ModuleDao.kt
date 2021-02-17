package com.mymasimo.masimosleep.data.room.dao

import androidx.room.*
import com.mymasimo.masimosleep.data.room.entity.Module
import com.mymasimo.masimosleep.data.room.entity.ModuleContract
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ModuleDao {

    @get:Query("SELECT * FROM ${ModuleContract.TABLE_NAME}")
    val allModulesUpdates: Flowable<List<Module>>

    @Query("SELECT COUNT(${ModuleContract.ID}) FROM ${ModuleContract.TABLE_NAME}")
    fun countModules(): Single<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg modules: Module): LongArray

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(module: Module): Int

    @Delete
    fun delete(module: Module): Int

    @Query("DELETE FROM ${ModuleContract.TABLE_NAME} WHERE ${ModuleContract.ID}=:id")
    fun delete(id: Long): Int

    @Query("SELECT * FROM ${ModuleContract.TABLE_NAME} WHERE ${ModuleContract.ID}=:moduleId LIMIT 1")
    fun getModule(moduleId: Long): Maybe<Module>

    @Query("SELECT * FROM ${ModuleContract.TABLE_NAME} WHERE ${ModuleContract.ID}=:moduleId LIMIT 1")
    fun getModuleUpdates(moduleId: Long): Observable<Module>

    @Query("SELECT ${ModuleContract.ID} FROM ${ModuleContract.TABLE_NAME} ORDER BY ${ModuleContract.ID} DESC LIMIT 1")
    fun getNextSelectedId(): Maybe<Long>
}
