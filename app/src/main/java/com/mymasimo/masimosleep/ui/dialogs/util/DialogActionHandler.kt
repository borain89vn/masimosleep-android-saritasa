package com.mymasimo.masimosleep.ui.dialogs.util

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogActionHandler @Inject constructor() {
    private val _lowBatteryDialogDismissals = PublishRelay.create<Unit>()
    val lowBatteryDialogDismissals: Observable<Unit>
        get() = _lowBatteryDialogDismissals

    private val _actions = PublishRelay.create<Action>()
    val actions: Observable<Action>
        get() = _actions

    fun onLowBatteryDialogDismissed() {
        _lowBatteryDialogDismissals.accept(Unit)
    }

    fun onEndSleepSessionClicked() {
        _actions.accept(Action.EndSessionClicked)
    }

    fun onEndSessionConfirmationClicked() {
        _actions.accept(Action.EndSessionConfirmationClicked)
    }

    fun onCancelSessionConfirmationClicked() {
        _actions.accept(Action.CancelSessionConfirmationClicked)
    }

    fun onSetUpDeviceNowClicked() {
        _actions.accept(Action.SetUpDeviceNowClicked)
    }

    fun onEndProgramConfirmationClicked() {
        _actions.accept(Action.EndProgramConfirmationClicked)
    }

    fun onConfirmReplaceSensorClicked() {
        _actions.accept(Action.ConfirmReplaceSensorClicked)
    }

    sealed class Action {
        object EndSessionClicked : Action()
        object EndSessionConfirmationClicked : Action()
        object CancelSessionConfirmationClicked : Action()
        object SetUpDeviceNowClicked : Action()
        object EndProgramConfirmationClicked : Action()
        object ConfirmReplaceSensorClicked : Action()
    }
}
