package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.SleepEventEntityDao
import com.mymasimo.masimosleep.data.room.entity.SleepEventEntity
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepEventRepository @Inject constructor(
    private val sleepEventEntityDao: SleepEventEntityDao,
    private val parameterReadingRepository: ParameterReadingRepository,
    private val programRepository: ProgramRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val sessionRepository: SessionRepository
) {
    fun saveSleepEvent(startAt: Long, endAt: Long, type: SleepEventType) {
        parameterReadingRepository.latestSPO2Reading()
            .flatMapCompletable { spo2 ->
                sleepEventEntityDao.insert(
                    SleepEventEntity(
                        spO2 = spo2,
                        startTime = startAt,
                        endTime = endAt,
                        type = type
                    )
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onComplete = {
                    Timber.d("Sleep event $type saved to the DB")
                },
                onError = { e ->
                    Timber.d(e, "Failed to save sleep event $type to the DB")
                }
            )
            .addTo(disposables)
    }

    fun sleepEventUpdates(startAt: Long): Observable<List<SleepEventEntity>> {
        return sleepEventEntityDao.findAllAfterTimeUpdates(startAt)
    }

    fun getAllEventsInProgramBySession(programId: Long): Single<List<SleepEventEntity>> {
        return programRepository.getProgram(programId)
            .flatMap { program ->
                val start = program.startDate
                val end = program.endDate ?: Calendar.getInstance().timeInMillis
                sleepEventEntityDao.findAllBetweenTimestamps(start, end)
            }
    }

    fun getAllEventsInSession(sessionId: Long): Single<List<SleepEventEntity>> {
        return sessionRepository.getSessionById(sessionId)
            .flatMap { session ->
                val endAt = session.endAt
                    ?: throw IllegalStateException("Session doesn't have an endAt")
                sleepEventEntityDao.findAllBetweenTimestamps(session.startAt, endAt)
            }
    }

    fun getCountOfAllEventsInSession(sessionId: Long): Single<Int> {
        return sessionRepository.getSessionById(sessionId)
            .flatMap { session ->
                val endAt = session.endAt
                    ?: throw IllegalStateException("Session doesn't have an endAt")
                return@flatMap sleepEventEntityDao.countAllBetweenTimestamps(session.startAt, endAt)
            }
    }
}