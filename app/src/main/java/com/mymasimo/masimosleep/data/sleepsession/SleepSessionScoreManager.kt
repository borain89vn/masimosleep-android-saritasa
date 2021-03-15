package com.mymasimo.masimosleep.data.sleepsession

import com.jakewharton.rxrelay2.BehaviorRelay
import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreObserver
import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreProvider
import com.masimo.sleepscore.sleepscorelib.model.Parameter
import com.masimo.sleepscore.sleepscorelib.model.SleepEvent
import com.masimo.sleepscore.sleepscorelib.model.SleepImprovementResult
import com.masimo.sleepscore.sleepscorelib.model.SleepSessionScore
import com.mymasimo.masimosleep.BuildConfig
import com.mymasimo.masimosleep.MasimoSleepApp
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.repository.*
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.data.room.entity.ScoreType
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.service.DeviceExceptionHandler
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepSessionScoreManager @Inject constructor(
    private val scoreRepository: SleepScoreRepository,
    private val sessionRepository: SessionRepository,
    private val programRepository: ProgramRepository,
    private val parameterReadingRepository: ParameterReadingRepository,
    private val sleepEventRepository: SleepEventRepository,
    private val deviceExceptionHandler: DeviceExceptionHandler
) {
    private var maxSleepHourDisposable: Disposable? = null

    private val scoreObserver = object : SleepSessionScoreObserver {
        override fun handle(sleepEvent: SleepEvent) {
            Timber.d("Sleep event received: $sleepEvent")
            if (sleepEvent.type == com.masimo.sleepscore.sleepscorelib.model.SleepEventType.NONE) {
                Timber.d("Ignoring sleep event of type NONE")
                return
            }
            sleepEventRepository.saveSleepEvent(
                startAt = sleepEvent.epochStartTime,
                endAt = sleepEvent.epochEndTime,
                type = SleepEventType.fromMasimoType(sleepEvent.type)
            )
        }

        override fun handle(sleepImprovementResult: SleepImprovementResult, id: Long) {
            if (sleepImprovementResult.isValid) {
                programRepository.setProgramOutcomeOfProgram(sleepImprovementResult.value.toDouble(), id)
            } else {
                programRepository.setProgramOutcomeOfProgram(0.0, id)
                Timber.e("Received invalid SleepImprovementResult!")
            }
        }

        override fun handle(sleepScore: SleepSessionScore) {
            val scoreValue = sleepScore.value.toDouble()
            val scoreType = ScoreType.fromSleepSessionScoreType(sleepScore.type)
            Timber.d("$scoreType sleep score received with value: $scoreValue")

            when (ScoreType.fromSleepSessionScoreType(sleepScore.type)) {
                ScoreType.LIVE -> {
                    if (isSessionInProgress) {
                        scoreRepository.saveLiveScore(sleepScore.value.toDouble())
                    }
                }
                ScoreType.SESSION -> {
                    scoreRepository.saveSessionScore(sleepScore.value.toDouble(), sleepScore.id)
                }
                ScoreType.PROGRAM -> {
                    programRepository.setProgramScoreOfLatestProgram(scoreValue, sleepScore.id)
                }
            }
        }

        override fun sessionEnded() {
            Timber.d("Sleep session ended")
        }

        override fun sessionStarted() {
            Timber.d("Sleep session started")
        }
    }

    private var currentNight: Int? = null
        set(value) {
            if (field != value) {
                field = value
                isSessionInProgressRelay.accept(currentNight != null)
            }
        }
    val isSessionInProgressRelay = BehaviorRelay.create<Boolean>()

    private var currentNightStartAt: Long? = null
    private var currentNightEndAt: Long? = null
    val isSessionInProgress: Boolean
        get() = currentNight != null
    val currentSessionStartAt: Long
        get() = currentNightStartAt ?: throw IllegalStateException("No session in progress")

    /**
     * Whether a reading of each of the parameters has been read and saved in the current session.
     * Set to [true] when the first reading comes in and [false] when the session ends.
     */
    private var hasReadSPO2ThisSession = false
    private var hasReadPRThisSession = false
    private var hasReadRRPThisSession = false

    init {
        SleepSessionScoreProvider.subScribeObserver(scoreObserver)
    }

    fun startSession(nightNumber: Int): Long {
        if (currentNight != null || currentNightStartAt != null || currentNightEndAt != null) {
            throw IllegalStateException("Cannot start session without ending the one in progress")
        }
        Timber.d("Starting session $nightNumber")
        SleepSessionScoreProvider.startSession()
        SleepSessionScoreProvider.resetSession()

        val now = Calendar.getInstance().timeInMillis
        currentNight = nightNumber
        currentNightStartAt = now

        sessionRepository.saveSession(nightNumber, now)

        startMaxHourSleepCountDown(nightNumber, now)

        return now
    }

    fun resumeSession(nightNumber: Int, startAt: Long): Long {
        Timber.d("Resuming session $nightNumber")
        SleepSessionScoreProvider.startSession()
        SleepSessionScoreProvider.resetSession()

        val now = Calendar.getInstance().timeInMillis
        currentNight = nightNumber
        currentNightStartAt = startAt

        startMaxHourSleepCountDown(nightNumber, startAt)

        return now
    }

    /**
     * Auto-end the session if the current session duration is longer than 12 hours(checking every minute)
     */
    private fun startMaxHourSleepCountDown(nightNumber: Int, startAt: Long) {
        stopMaxHourSleepCountDown()
        maxSleepHourDisposable = Observable
            .interval(1, TimeUnit.SECONDS)
            .doOnNext {
                if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startAt) > MasimoSleepApp.get().resources.getInteger(R.integer.max_sleep_time).toLong()) {
                    if (isSessionInProgress && nightNumber == currentNight) {
                        //current night is still in progress
                        Timber.d("Current sleep exceeds 12 hours, ending sleep session")
                        endSession(SessionTerminatedCause.ENOUGH_SLEEP_ENDED)
                    }
                }
            }.subscribe()
    }

    private fun stopMaxHourSleepCountDown() {
        maxSleepHourDisposable?.dispose()
        maxSleepHourDisposable = null
    }

    fun isSessionDurationValid(): Boolean {
        val sessionStartAt = currentSessionStartAt
        val nowMillis = Calendar.getInstance().timeInMillis
        val diffMillis = nowMillis - sessionStartAt
        val thresholdMillis = 2 * 60 * 60 * 1000 // 2 hours.

        // Allow less than 2 hours sleep for debug
        @Suppress("ConstantConditionIf")
        if (BuildConfig.ALLOW_SHORT_SLEEP) {
            return true
        }

        return diffMillis > thresholdMillis
    }

    fun endSession(sessionTerminatedCause: SessionTerminatedCause?) {
        if (!isSessionInProgress) {
            throw IllegalStateException("No session in progress")
        }

        currentNightEndAt = Calendar.getInstance().timeInMillis

        val currentNight = this.currentNight
            ?: throw IllegalStateException("Current night should be set")
        val currentNightStartAt = this.currentNightStartAt
            ?: throw IllegalStateException("Current night start should be set")
        val currentNightEndAt = this.currentNightEndAt
            ?: throw IllegalStateException("Current night end should be set")

        Timber.d("Ending session $currentNight - start=$currentNightStartAt end=$currentNightEndAt")
        sessionRepository.endCurrentSession(currentNightEndAt, sessionTerminatedCause)

        resetCurrentSession()
    }

    fun cancelSession(sessionTerminatedCause: SessionTerminatedCause?) {
        if (!isSessionInProgress) {
            throw IllegalStateException("No session in progress")
        }

        val currentNight = this.currentNight
            ?: throw IllegalStateException("Current night should be set")
        val currentNightStartAt = this.currentNightStartAt
            ?: throw IllegalStateException("Current night start should be set")

        Timber.d("Canceling session $currentNight - start=$currentNightStartAt")

        sessionRepository.cancelCurrentSession(sessionTerminatedCause, this)

        resetCurrentSession()
    }

    /**
     * This function should be called whenever sensor can't provide 1Hz data during a session
     */
    fun interrupt() {
        SleepSessionScoreProvider.interrupt()
    }

    fun sendTick(pSPO2: Parameter, pPR: Parameter, pRRP: Parameter) {
        if (pSPO2.value == 0f && pPR.value == 0f && pRRP.value == 0f) {
            Timber.d("Ignoring tick with all values in 0f")
            return
        }
        Timber.d("New tick: pSPO2=${pSPO2.value} pPR=${pPR.value} pRRP=${pRRP.value}")
        SleepSessionScoreProvider.tick(arrayOf(pSPO2, pPR, pRRP), System.currentTimeMillis())

        synchronized(this) {
            if (!isSessionInProgress) {
                Timber.d("Not saving reading until a session has started")
                return
            }

            Timber.d("Saving reading of pSPO2=${pSPO2.value != 0f} pPR=${pPR.value != 0f} pRRP=${pRRP.value != 0f}")

            if (pSPO2.value != 0f) {
                parameterReadingRepository.saveReading(
                    ReadingType.SP02,
                    pSPO2.value,
                    insertWithBatch = hasReadSPO2ThisSession // Save immediately if first reading.
                )
                hasReadSPO2ThisSession = true
            }
            if (pPR.value != 0f) {
                parameterReadingRepository.saveReading(
                    ReadingType.PR,
                    pPR.value,
                    insertWithBatch = hasReadPRThisSession // Save immediately if first reading.
                )
                hasReadPRThisSession = true
            }
            if (pRRP.value != 0f) {
                parameterReadingRepository.saveReading(
                    ReadingType.RRP,
                    pRRP.value,
                    insertWithBatch = hasReadRRPThisSession // Save immediately if first reading.
                )
                hasReadRRPThisSession = true
            }
        }
    }

    private fun resetCurrentSession() {
        synchronized(this) {
            currentNight = null
            currentNightStartAt = null
            currentNightEndAt = null
            hasReadSPO2ThisSession = false
            hasReadPRThisSession = false
            hasReadRRPThisSession = false
            stopMaxHourSleepCountDown()
            stopDisconnectionEndSessionCountDown()
            stopSensorOffEndSessionCountDown()
            deviceExceptionHandler.clearExceptions()
        }
    }

    private var bleDisconnectionEndSessionCountDownDisposable: Disposable? = null

    /**
     * Auto-end session if sensor is OFF for more than 30 minutes
     */
    fun startDisconnectionEndSessionCountDown() {
        val observable = Observable.timer(MasimoSleepApp.get().resources.getInteger(R.integer.max_ble_disconnect_time).toLong(), TimeUnit.SECONDS)
            .doOnNext {
                if (isSessionInProgress) {
                    if (isSessionDurationValid()) {
                        endSession(SessionTerminatedCause.SENSOR_DISCONNECTED_ENDED)
                    } else {
                        cancelSession(SessionTerminatedCause.SENSOR_DISCONNECTED_CANCELLED)
                    }
                }
            }
            .doOnSubscribe {
                bleDisconnectionEndSessionCountDownDisposable = it
            }

        bleDisconnectionEndSessionCountDownDisposable ?: run {
            observable.subscribe()
        }
    }

    fun stopDisconnectionEndSessionCountDown() {
        bleDisconnectionEndSessionCountDownDisposable?.dispose()
        bleDisconnectionEndSessionCountDownDisposable = null
    }

    private var sensorOffEndSessionCountDownDisposable: Disposable? = null

    /**
     * Auto-end session if sensor is OFF for more than 30 minutes
     */
    fun startSensorOffEndSessionCountDown() {
        val observable = Observable.timer(MasimoSleepApp.get().resources.getInteger(R.integer.max_sensor_off_time).toLong(), TimeUnit.SECONDS)
            .doOnNext {
                if (isSessionInProgress) {
                    if (isSessionDurationValid()) {
                        endSession(SessionTerminatedCause.SENSOR_OFF_ENDED)
                    } else {
                        cancelSession(SessionTerminatedCause.SENSOR_OFF_CANCELED)
                    }
                }
            }
            .doOnSubscribe {
                sensorOffEndSessionCountDownDisposable = it
            }

        sensorOffEndSessionCountDownDisposable ?: run {
            observable.subscribe()
        }
    }

    fun stopSensorOffEndSessionCountDown() {
        sensorOffEndSessionCountDownDisposable?.dispose()
        sensorOffEndSessionCountDownDisposable = null
    }
}
