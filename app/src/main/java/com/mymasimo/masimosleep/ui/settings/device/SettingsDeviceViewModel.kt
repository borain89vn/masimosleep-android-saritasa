package com.mymasimo.masimosleep.ui.settings.device

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SensorRepository
import com.mymasimo.masimosleep.service.serviceDisconnectBLE
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsDeviceViewModel @Inject constructor(
    app: Application,
    schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val sensorRepository: SensorRepository,
    dialogActionHandler: DialogActionHandler,
) : AndroidViewModel(app) {
    private val _confirmReplaceDevice = PublishRelay.create<Unit>()
    val confirmReplaceDevice: Observable<Unit>
        get() = _confirmReplaceDevice

    private val _deviceDeleted = PublishRelay.create<Unit>()
    val deviceDeleted: Observable<Unit>
        get() = _deviceDeleted

    init {
        dialogActionHandler.actions
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { action ->
                when (action) {
                    DialogActionHandler.Action.ConfirmReplaceSensorClicked -> onConfirmReplaceDevice()
                }
            }
            .addTo(disposables)
    }

    private fun onConfirmReplaceDevice() {
        _confirmReplaceDevice.accept(Unit)
    }

    fun deleteDevice() = viewModelScope.launch {
        val sensorId = sensorRepository.getSelectedSensorId()
        sensorRepository.deleteSensor(sensorId)
        serviceDisconnectBLE(getApplication(), sensorId)
        _deviceDeleted.accept(Unit)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
