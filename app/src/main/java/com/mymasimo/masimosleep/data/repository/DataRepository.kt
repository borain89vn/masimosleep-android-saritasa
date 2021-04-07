package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.MasimoSleepApp
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.room.ModulesDatabase
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

object DataRepository {

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

