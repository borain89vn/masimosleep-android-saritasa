package com.mymasimo.masimosleep.ui.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.service.*
import com.mymasimo.masimosleep.service.DeviceException.LOW_BATTERY
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler.Action
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    sleepScoreRepository: SleepScoreRepository,
    deviceExceptionHandler: DeviceExceptionHandler,
    dialogActionHandler: DialogActionHandler,
    bleConnectionState: BLEConnectionState,
    programRepository: ProgramRepository,
    private val disposables: CompositeDisposable,
    private val sleepSessionScoreManager: SleepSessionScoreManager
) : ViewModel() {

    private var hasBatteryLowWarningBeenShown = false

    private val _scoreAvailable = MutableLiveData(false)
    val scoreAvailable: LiveData<Boolean>
        get() = _scoreAvailable

    val sessionEnded = PublishRelay.create<Pair<Long, Int>>()

    val sessionCanceled = PublishRelay.create<Long>()

    private val _showEndSessionConfirmation = PublishRelay.create<Unit>()
    val showEndSessionConfirmation: Observable<Unit>
        get() = _showEndSessionConfirmation

    private val _showCancelSessionConfirmation = PublishRelay.create<Unit>()
    val showCancelSessionConfirmation: Observable<Unit>
        get() = _showCancelSessionConfirmation

    private var showingDeviceException: DeviceException? = null
    private var showingBleDialog = false

    private val _showSensorDialog = PublishRelay.create<DeviceException>()
    val showSensorDialog: Observable<DeviceException>
        get() = _showSensorDialog

    private val _removeSensorDialog = PublishRelay.create<DeviceException>()
    val removeSensorDialog: Observable<DeviceException>
        get() = _removeSensorDialog

    private val _showBleDialog = PublishRelay.create<Unit>()
    val showBleDialog: Observable<Unit>
        get() = _showBleDialog

    private val _hideDialogs = PublishRelay.create<Unit>()
    val hideDialogs: Observable<Unit>
        get() = _hideDialogs

    private val _sessionInProgress = PublishRelay.create<Boolean>()
    val sessionInProgress: Observable<Boolean>
        get() = _sessionInProgress

    init {
        sleepScoreRepository.onLiveScoreSaved
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                // Only update it if it's null or false.
                if (_scoreAvailable.value != true) {
                    _scoreAvailable.value = true
                }
            }
            .addTo(disposables)

        sessionRepository.onSessionEndedUpdates
            .flatMap { sessionId ->
                programRepository.getCurrentProgram()
                    .flatMap { program ->
                        sessionRepository.countAllSessionsInProgram(program.id ?: throw IllegalStateException())
                    }
                    .map { currentNight -> Pair(sessionId, currentNight) }
                    .toObservable()
            }
            .take(1)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ (sessionId, currentNight) ->
                sessionEnded.accept(Pair(sessionId, currentNight))
            }, {
                it.printStackTrace()
            })
            .addTo(disposables)

        sessionRepository.onSessionCanceledUpdates
            .take(1)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { sessionId ->
                sessionCanceled.accept(sessionId)
            }
            .addTo(disposables)

        dialogActionHandler.lowBatteryDialogDismissals
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                showingDeviceException = null
            }
            .addTo(disposables)

        dialogActionHandler.actions
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { action ->
                when (action) {
                    Action.EndSessionClicked -> onEndSessionClick()
                    Action.EndSessionConfirmationClicked -> onEndSessionConfirmed()
                    Action.CancelSessionConfirmationClicked -> onCancelSessionConfirmed()
                }
            }
            .addTo(disposables)

        bleConnectionState.currentState
            .map { state -> state == State.DEVICE_CONNECTED }
            .doOnNext { state -> Timber.d("BLE connection state changed to $state") }
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { isConnected ->
                Timber.d("state change isConnected=$isConnected hiding dialogs..")
                _hideDialogs.accept(Unit)
                showingDeviceException = null
                showingBleDialog = false
                if (!isConnected) {
                    if (!sleepSessionScoreManager.isSessionInProgress) return@subscribe
                    showingBleDialog = true
                    _showBleDialog.accept(Unit)
                    Timber.d("showing disconnected dialog...")
                }
            }
            .addTo(disposables)

        deviceExceptionHandler.exceptionUpdates
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { deviceExceptions ->
                if (!sleepSessionScoreManager.isSessionInProgress) return@subscribe
                if (showingBleDialog) return@subscribe

                val isShowingDialog = showingDeviceException != null
                if (isShowingDialog) {
                    // Already showing something, check if it should be removed.
                    val showingType = showingDeviceException ?: throw IllegalStateException()
                    // It should be removed if it is no longer on the exception list and is not of
                    // the low battery type, which is only dismissible via user action.
                    val shouldBeRemoved = !deviceExceptions.contains(showingType)
                            && showingType != LOW_BATTERY
                    if (shouldBeRemoved) {
                        _removeSensorDialog.accept(showingType)
                        showingDeviceException = null
                    }
                } else {
                    // Not showing anything, should show a dialog for the first exception.
                    if (deviceExceptions.isNotEmpty()) {
                        if (deviceExceptions.first() == LOW_BATTERY) {
                            // The first exception is for low battery, check we haven't shown it
                            // before.
                            if (!hasBatteryLowWarningBeenShown) {
                                // We haven't shown it before, show it now.
                                _showSensorDialog.accept(LOW_BATTERY)
                                showingDeviceException = LOW_BATTERY
                                hasBatteryLowWarningBeenShown = true
                            } else {
                                // We've already shown the battery dialog once this session, don't
                                // show it again but do show one for the next exception if there's
                                // one available.
                                val candidateExceptions = deviceExceptions
                                    .filterNot { it == LOW_BATTERY }
                                if (candidateExceptions.isNotEmpty()) {
                                    // We'll show the next exception that's not low battery.
                                    _showSensorDialog.accept(candidateExceptions.first())
                                    showingDeviceException = candidateExceptions.first()
                                }
                            }
                        } else {
                            // Show the exception that's not low battery.
                            _showSensorDialog.accept(deviceExceptions.first())
                            showingDeviceException = deviceExceptions.first()
                        }
                    }
                }
            }
            .addTo(disposables)
    }

    fun onEndSessionClick() {
        if (sleepSessionScoreManager.isSessionDurationValid()) {
            _showEndSessionConfirmation.accept(Unit)
        } else {
            _showCancelSessionConfirmation.accept(Unit)
        }
    }

    private fun onCancelSessionConfirmed() {
        sleepSessionScoreManager.cancelSession(null)
    }

    private fun onEndSessionConfirmed() {
        sleepSessionScoreManager.endSession(null)
    }

    fun checkBLEState() {
        if (isDeviceConnected()) {
            _hideDialogs.accept(Unit)
            showingDeviceException = null
            showingBleDialog = false
        } else {
            showingBleDialog = true
            _showBleDialog.accept(Unit)
        }
    }

    fun checkSessionInProgressState() {
        sessionRepository.getSessionInProgress()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onSuccess = { _sessionInProgress.accept(true) },
                onComplete = { _sessionInProgress.accept(false) },
                onError = { _sessionInProgress.accept(false) }
            ).addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
