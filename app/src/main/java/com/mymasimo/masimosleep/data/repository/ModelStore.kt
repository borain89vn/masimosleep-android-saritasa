package com.mymasimo.masimosleep.data.repository

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.data.room.entity.Module
import io.reactivex.Observable

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
}