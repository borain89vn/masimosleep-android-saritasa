package com.mymasimo.masimosleep.data.repository

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.room.entity.Module
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

// TODO: 3/15/21 Rewrite to repository for modules
object ModelStore {

    val currentModuleUpdates: MutableLiveData<Module> = MutableLiveData()

    // Emits whenever the module is loaded on startup.
    private val onStartUpModuleLoadedRelay = PublishRelay.create<Unit>()
    val onStartUpModuleLoaded: Observable<Unit>
        get() = onStartUpModuleLoadedRelay

    var currentModule: Module? = null
        set(value) {
            if (value == field) return

            field = value
            currentModuleUpdates.value = value
        }

    init {
        val moduleId = MasimoSleepPreferences.selectedModuleId
        if (moduleId > 0) {
            // If there's a saved module load it.
            loadModule(moduleId)
        } else {
            currentModule = null
            Timber.d("No paired module")
        }
    }

    fun loadModule(moduleId: Long) {
        // TODO: This needs to be disposed.
        Timber.d("Loading module with id: $moduleId")
        DataRepository.observeModule(moduleId)
            .observeOn(Schedulers.trampoline())
            .subscribe(
                { module ->
                    Timber.d("Current module loaded: $module")
                    currentModule = module
                    onStartUpModuleLoadedRelay.accept(Unit)
                },
                { Timber.e(it, "Unable to load module with id $moduleId") }
            )
    }
}