package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.MasimoSleepApp
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.room.ModulesDatabase
import com.mymasimo.masimosleep.data.room.entity.Module
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

object DataRepository {

    val modulesSubscription: Flowable<List<Module>>
        get() = ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao().allModulesUpdates
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnError { it.printStackTrace() }
            .doOnNext {
                Timber.d("Modules Update Received, Size = %d", it.size)
            }

    /**
     * First Attempts to get a Module with the Same Type. If the module of same type exists updates
     * the new modules ModuleID before adding so that it replaces that module. If no existing module
     * is found with the same type then the new module is added with an auto-generated module id.
     *
     *
     * Two Database Operations: Select Query + Insert(Replace)
     *
     * @param module
     * @return
     */
    fun addModule(module: Module) = Completable.create {
        try {
            module.id.let { mid ->
                val id = if (mid == null) {
                    Timber.d("Inserting")
                    ModulesDatabase.getInstance(MasimoSleepApp.get())
                        .moduleDao().insert(module).first()
                } else {
                    val count = ModulesDatabase.getInstance(MasimoSleepApp.get())
                        .moduleDao().update(module)
                    Timber.d("Updated $count rows")
                    mid
                }

                module.id = id
                Timber.d("Inserted ${module.type}|${module.variant} at row ${module.id}")
                it.onComplete()
                MasimoSleepPreferences.selectedModuleId = id
            }
        } catch (e: Exception) {
            it.onError(e)
        }
    }

    fun getModule(id: Long): Maybe<Module> = Maybe.create {
        if (id == 0L) {
            it.onComplete()
            Timber.d("No module for id 0")
            return@create
        }

        Timber.d("Querying module id=$id")
        ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao()
            .getModule(id)
            .doOnComplete {
                Timber.d("Load complete")
                it.onComplete()
            }
            .doOnSuccess { m ->
                Timber.d("Load successful: $m")
                it.onSuccess(m)
            }
            .doOnError { e -> it.onError(e) }
            .subscribe()
    }

    fun observeModule(id: Long): Observable<Module> {
        return ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao()
            .getModuleUpdates(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun observeModules(): Flowable<List<Module>> {
        return ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao()
            .allModulesUpdates
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun insertModule(module: Module): Completable? {
        return Completable.fromAction {
            ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao().insert(module) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnComplete { Timber.d("Successfully inserted Module ${module.id} of Type: ${module.type}|${module.variant}") }
    }

    fun deleteModule(module: Module) = Single.fromCallable {
        ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao().delete(module)
        Timber.d("Successfully deleted Module ${module.id} of Type ${module.type}|${module.variant}")
        runAfterModuleDelete(module.id)
    }

    fun deleteModule(id: Long) = Single.fromCallable {
        val rowsAffected = ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao().delete(id)
        Timber.d("Deleted $rowsAffected modules (id=$id)")
        runAfterModuleDelete(id)
        return@fromCallable rowsAffected
    }

    private fun runAfterModuleDelete(deletedModuleId: Long?) {
        // nothing to do if the selected module wasn't deleted
        if (deletedModuleId != MasimoSleepPreferences.selectedModuleId) {
            Timber.d("Selected module not deleted. Not updating selectedModuleId.")
            return
        }

        // replace the selected module id with the next
        ModulesDatabase.getInstance(MasimoSleepApp.get()).moduleDao()
            .getNextSelectedId()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .subscribe(object : MaybeObserver<Long> {
                var id = 0L

                override fun onSuccess(t: Long) {
                    Timber.d("Next module id: $t")
                    id = t
                }

                override fun onComplete() {
                    MasimoSleepPreferences.selectedModuleId = id

                    Timber.d("Completed. Selected module id: ${MasimoSleepPreferences.selectedModuleId}")
                }

                override fun onSubscribe(d: Disposable) {
                    Timber.d("Updating selected module id")
                }

                override fun onError(e: Throwable) {
                    Timber.e(e, "Error getting next selected module id")
                    MasimoSleepPreferences.selectedModuleId = 0L
                }
            })
    }
}

