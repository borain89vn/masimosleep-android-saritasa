package com.mymasimo.masimosleep.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.alarm.SleepReminderAlarmScheduler
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.room.dao.ProgramEntityDao
import com.mymasimo.masimosleep.data.room.entity.ProgramEntity
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val dialogActionHandler: DialogActionHandler,
    private val sleepReminderAlarmScheduler: SleepReminderAlarmScheduler,
    private val sleepSessionScoreManager: SleepSessionScoreManager,
    private val programEntityDao: ProgramEntityDao
) : ViewModel() {

    private val _programState = MutableLiveData<ProgramState>(ProgramState.NoProgramInProgress)
    val programState: LiveData<ProgramState>
        get() = _programState

    private val _sessionInProgress = PublishRelay.create<SessionEntity>()
    val sessionInProgress: Observable<SessionEntity>
        get() = _sessionInProgress

    private val _sessionConfiguration = MutableLiveData<SessionConfiguration>()
    val sessionConfiguration: LiveData<SessionConfiguration>
        get() = _sessionConfiguration

    private val _showSetupDeviceDialog = PublishRelay.create<Unit>()
    val showSetupDeviceDialog: Observable<Unit>
        get() = _showSetupDeviceDialog

    private val _goToDeviceSetupFlow = PublishRelay.create<Unit>()
    val goToDeviceSetupFlow: Observable<Unit>
        get() = _goToDeviceSetupFlow

    val sessionEnded = PublishRelay.create<Pair<Long, Int>>()

    val sessionCanceled = PublishRelay.create<Long>()

    private var sessionIdToAutoSelect: Long? = null

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun setDefaultSessionIdToOpen(sessionId: Long) {
        if (sessionId > 0) {
            sessionIdToAutoSelect = sessionId
        }
    }

    fun onViewCreated() {
        loadProgram()

        dialogActionHandler.actions
            .filter { it is DialogActionHandler.Action.SetUpDeviceNowClicked }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                _goToDeviceSetupFlow.accept(Unit)
            }
            .addTo(disposables)

        if (MasimoSleepPreferences.selectedModuleId <= 0L) {
            _showSetupDeviceDialog.accept(Unit)
        }
    }

    fun getCurrentNight(): Int {
        val program = programState.value ?: return -1
        if (program !is ProgramState.ProgramInProgress) return -1

        return program.currentNight
    }

    fun onNightSelected(night: Int) {
        val program = programState.value ?: return
        if (program !is ProgramState.ProgramInProgress) return

        val newConfiguration = if (program.currentNight == night) {
            SessionConfiguration.Today
        } else {
            val sessionId = program.sessions[night - 1].id ?: throw IllegalStateException()
            SessionConfiguration.Summary(sessionId, night)
        }

        if (sessionConfiguration.value != newConfiguration) {
            _sessionConfiguration.value = newConfiguration
        }
    }

    private fun endProgram(programId: Long): Completable {
        return programEntityDao.findById(programId)
            .flatMapCompletable { program ->
                program.endDate = Calendar.getInstance().timeInMillis
                programEntityDao.update(program)
            }
    }

    private fun loadProgram() {
        disposables.clear()
        programRepository.getCurrentProgramIfExists()
            .flatMap { program ->
                val programId = program.id ?: throw IllegalStateException()
                return@flatMap sessionRepository.getAllSessionsByProgramIdAsc(programId)
                    .flatMap { sessions ->
                        if (sessions.size >= NUM_OF_NIGHTS) {
                            endProgram(programId).toSingle { sessions }
                        } else {
                            Single.just(sessions)
                        }
                    }
                    .map { sessions ->
                        if (sessions.size >= NUM_OF_NIGHTS) {
                            return@map ProgramState.NoProgramInProgress
                        }

                        val autoSelectSession =
                            if (_sessionConfiguration.value is SessionConfiguration.Summary) (_sessionConfiguration.value as SessionConfiguration.Summary).sessionId else sessionIdToAutoSelect
                        val currentNight = when {
                            sleepSessionScoreManager.isSessionInProgress -> sessions.size
                            else -> sessions.size + 1
                        }

                        val selectedNight: Int
                        val selectedSessionId: Long?
                        if (autoSelectSession == null) {
                            // Default selection to the current night.
                            selectedNight = currentNight
                            selectedSessionId = null
                        } else {
                            selectedNight = sessions.indexOfFirst { it.id == autoSelectSession } + 1
                            selectedSessionId = autoSelectSession
                        }

                        sessionIdToAutoSelect = null

                        return@map ProgramState.ProgramInProgress(
                            program = program,
                            sessions = sessions,
                            currentNight = currentNight,
                            selectedNight = selectedNight,
                            selectedSessionId = selectedSessionId
                        )
                    }
                    .toMaybe()
            }
            .defaultIfEmpty(ProgramState.NoProgramInProgress)
            .doOnSuccess { programState ->
                when (programState) {
                    ProgramState.NoProgramInProgress -> {
                        sleepReminderAlarmScheduler.cancelAlarms()
                    }
                    is ProgramState.ProgramInProgress -> {
                        val remainingNights = NUM_OF_NIGHTS - programState.currentNight + 1
                        sleepReminderAlarmScheduler.scheduleAlarms(remainingNights)
                    }
                }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { programState ->
                _programState.value = programState

                when (programState) {
                    ProgramState.NoProgramInProgress -> {
                        _sessionConfiguration.value = SessionConfiguration.Today
                    }
                    is ProgramState.ProgramInProgress -> {
                        val isCurrentNightSelected =
                            programState.currentNight == programState.selectedNight


                        _sessionConfiguration.value = if (isCurrentNightSelected) {
                            SessionConfiguration.Today
                        } else {
                            SessionConfiguration.Summary(
                                programState.selectedSessionId ?: throw IllegalStateException(),
                                programState.selectedNight
                            )
                        }
                    }
                }
            }
            .addTo(disposables)

        sessionRepository.onSessionEndedUpdates
            .flatMap { sessionId ->
                programRepository.getCurrentProgram()
                    .flatMap { program ->
                        sessionRepository.countAllSessionsInProgram(
                            program.id ?: throw IllegalStateException()
                        )
                    }
                    .map { currentNight -> Pair(sessionId, currentNight) }
                    .toObservable()
            }
            .take(1)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                { (sessionId, currentNight) -> sessionEnded.accept(Pair(sessionId, currentNight)) },
                { it.printStackTrace() }
            )
            .addTo(disposables)

        sessionRepository.onSessionCanceledUpdates
            .take(1)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { sessionId -> sessionCanceled.accept(sessionId) }
            .addTo(disposables)

        sessionRepository.getSessionInProgress()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                { sessionEntity ->
                    if (sleepSessionScoreManager.isSessionInProgress)
                        _sessionInProgress.accept(sessionEntity)
                },
                { Timber.e(it) }
            )
            .addTo(disposables)
    }

    sealed class ProgramState {
        object NoProgramInProgress : ProgramState()
        data class ProgramInProgress(
            val program: ProgramEntity,
            val sessions: List<SessionEntity>,
            val currentNight: Int,
            val selectedNight: Int,
            val selectedSessionId: Long?
        ) : ProgramState()
    }

    sealed class SessionConfiguration {
        object Today : SessionConfiguration()
        data class Summary(val sessionId: Long, val nightNumber: Int) : SessionConfiguration()
    }
}