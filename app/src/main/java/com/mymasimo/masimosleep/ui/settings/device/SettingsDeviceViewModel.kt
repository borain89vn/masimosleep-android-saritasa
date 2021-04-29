package com.mymasimo.masimosleep.ui.settings.device

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SensorRepository
import com.mymasimo.masimosleep.service.isDeviceConnected
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

    private val _connect = MutableLiveData<Int>()
    val connect: LiveData<Int>
        get() = _connect

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
        val sensorId = sensorRepository.getCurrentSensor()?.id
        if (sensorId != null) {
            sensorRepository.deleteSensor(sensorId)
            serviceDisconnectBLE(getApplication(), sensorId)
            _deviceDeleted.accept(Unit)
        }
    }

    fun onConnectTap() = viewModelScope.launch {
        val currentSensor = sensorRepository.getCurrentSensor()

        val action = when {
            isDeviceConnected() -> R.id.action_settingsFragment_to_sensorAlreadyConnectedDialogFragment
            currentSensor != null -> R.id.action_settingsFragment_to_confirmReplaceSensorDialogFragment
            else -> R.id.action_settingsFragment_to_scanFragment
        }
        _connect.value = action
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
