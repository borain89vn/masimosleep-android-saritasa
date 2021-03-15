package com.mymasimo.masimosleep.ui.night_report.sleep_pattern

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import com.mymasimo.masimosleep.ui.night_report.sleep_pattern.util.SleepPatternViewData
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SleepPatternViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposable: CompositeDisposable
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("HH:mm:ss")

    private val _viewData = MutableLiveData<SleepPatternViewData>()
    val viewData: LiveData<SleepPatternViewData>
        get() = _viewData

    fun onCreatedWithProgramId(programId: Long) {
        onCreated(Single.just(programId))
    }

    fun onCreatedWithSessionId(sessionId: Long) {
        onCreated(
            sessionRepository.getSessionById(sessionId)
                .map { it.programId }
        )
    }

    private fun onCreated(getProgramIdSingle: Single<Long>) {
        getProgramIdSingle
            .flatMap { programId ->
                sessionRepository.getAllSessionsByProgramIdAsc(programId)
            }
            .map { sessions ->
                if (sessions.isEmpty()) {
                    return@map SleepPatternViewData(
                        lowMinutes = 0,
                        highMinutes = 0,
                        avgSleepStartAt = 0,
                        avgSleepEndAt = 0,
                        mostLateEndAt = 0,
                        sleepSessions = emptyList()
                    )
                }
                return@map createSleepPatternViewData(sessions)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { viewData ->
                _viewData.value = viewData
            }
            .addTo(disposable)
    }

    private fun generateAverageTime(forStartTime: Boolean, sessions: List<SessionEntity>) : Long {
        var sum = 0L
        sessions.forEach {
            val formatted = dateFormatter.format(if(forStartTime) it.startAt else it.endAt)

            val parts = formatted.split(":")

            sum += parts[0].toLong() * 60 * 60
            sum += parts[1].toLong() * 60
            sum += parts[2].toLong()
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        return (sum / sessions.size) * 1000 + today
    }

    private fun createSleepPatternViewData(sessions: List<SessionEntity>): SleepPatternViewData {
        val completedSessions = sessions.filter { it.endAt != null }

        var lowMinutes: Int = Int.MAX_VALUE
        var highMinutes: Int = Int.MIN_VALUE
        completedSessions.forEach { session ->
            val startAtMillis = session.startAt
            val endAtMillis = session.endAt ?: throw IllegalStateException()
            val diffMinutes = (endAtMillis - startAtMillis) / 1000 / 60
            if (diffMinutes < lowMinutes) {
                lowMinutes = diffMinutes.toInt()
            }
            if (diffMinutes > highMinutes) {
                highMinutes = diffMinutes.toInt()
            }
        }


        val avgSleepStartAt = generateAverageTime(forStartTime = true, sessions = completedSessions)

        val avgSleepEndAt = generateAverageTime(forStartTime = false, sessions = completedSessions)

        val mostLateEndAt = completedSessions.maxByOrNull {
            it.endAt ?: throw IllegalStateException()
        }?.endAt ?: throw IllegalStateException()

        val sleepSessions = completedSessions.mapIndexed { index, session ->
            SleepPatternViewData.SleepSession(
                night = index + 1,
                startAt = session.startAt,
                endAt = session.endAt ?: throw IllegalStateException()
            )
        }

        return SleepPatternViewData(
            lowMinutes = lowMinutes,
            highMinutes = highMinutes,
            avgSleepStartAt = avgSleepStartAt,
            avgSleepEndAt = avgSleepEndAt,
            mostLateEndAt = mostLateEndAt,
            sleepSessions = sleepSessions
        )
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}