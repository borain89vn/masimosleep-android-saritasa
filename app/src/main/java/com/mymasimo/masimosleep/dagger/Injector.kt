package com.mymasimo.masimosleep.dagger

import com.mymasimo.masimosleep.MasimoSleepApp

class Injector private constructor() {
    companion object {
        fun get(): SingletonComponent = MasimoSleepApp.get().component
    }
}
