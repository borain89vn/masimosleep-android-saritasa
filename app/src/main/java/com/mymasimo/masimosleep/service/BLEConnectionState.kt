package com.mymasimo.masimosleep.service

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BLEConnectionState @Inject constructor() {

    private val _currentState = BehaviorRelay.createDefault(State.NO_DEVICE_CONNECTED)
    val currentState: Observable<State>
        get() = _currentState

    fun setCurrentState(state: State) {
        _currentState.accept(state)
    }
}

enum class State {
    BLE_DISCONNECTED, NO_DEVICE_CONNECTED, CONNECTING_TO_DEVICE, DEVICE_CONNECTED, SEARCHING
}
