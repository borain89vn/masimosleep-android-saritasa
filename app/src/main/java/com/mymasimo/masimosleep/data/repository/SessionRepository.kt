package com.mymasimo.masimosleep.data.repository

import com.jakewharton.rxrelay2.PublishRelay
import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreProvider
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.ProgramEntityDao
import com.mymasimo.masimosleep.data.room.dao.SessionEntityDao
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionEntityDao: SessionEntityDao,
    private val scoreRepository: SleepScoreRepository,
    private val sessionTerminatedRepository: SessionTerminatedRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val programEntityDao: ProgramEntityDao
) {

    private val onSessionEndedRelay = PublishRelay.create<Long>()
    val onSessionEndedUpdates: Observable<Long>
        get() = onSessionEndedRelay

    private val onSessionCanceledRelay = PublishRelay.create<Long>()
    val onSessionCanceledUpdates: Observable<Long>
        get() = onSessionCanceledRelay

    fun getSessionInProgressId(): Single<Long> {
        return sessionEntityDao.findSessionInProgressId()
    }

    fun getSessionInProgress(): Single<SessionEntity> {
        return sessionEntityDao.findSessionInProgress()
    }

    fun getSessionById(sessionId: Long): Single<SessionEntity> {
        return sessionEntityDao.findSessionById(sessionId)
    }

    fun getAllSessionsByProgramIdAsc(programId: Long): Single<List<SessionEntity>> {
        return sessionEntityDao.findAllByProgramIdAsc(programId)
    }

    fun getLatestEndedSession(programId: Long) = sessionEntityDao.findLastEndedSessionForProgram(programId)

    fun getAllSessionsByProgramId(programId: Long): Single<List<SessionEntity>> {
        return sessionEntityDao.findAllByProgramId(programId)
    }

    fun countAllSessionsInProgram(programId: Long): Single<Int> {
        return sessionEntityDao.countAllByProgramId(programId)
    }

    fun saveSession(nightNumber: Int, startAt: Long) {
        // TODO: check there isn't a session with that nightNumber already.
        programEntityDao.findCurrentProgram()
            .flatMapCompletable { program ->
                sessionEntityDao.insert(
                    SessionEntity(
                        programId = program.id ?: throw IllegalStateException(),
                        nightNumber = nightNumber,
                        startAt = startAt
                    )
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onComplete = {
                    Timber.d("Session for night=$nightNumber started and saved to the DB with startAt=$startAt")
                },
                onError = { e ->
                    Timber.e(e, "Error saving session for night=$nightNumber")
                }
            )
            .addTo(disposables)
    }

    fun endCurrentSession(endAt: Long, cause: SessionTerminatedCause?) {
        sessionEntityDao.findSessionInProgress()
            .flatMap { sessionInProgress ->
                sessionInProgress.endAt = endAt
                return@flatMap sessionEntityDao.update(sessionInProgress)
                    .doOnComplete { Timber.d("Session end time saved") }
                    .toSingleDefault(sessionInProgress)
            }
            .flatMap { sessionJustEnded ->
                return@flatMap sessionTerminatedRepository.saveTerminatedCause(
                    sessionId = sessionJustEnded.id,
                    night = sessionJustEnded.nightNumber,
                    sessionTerminatedCause = cause,
                    recorded = true
                )
                    .doOnComplete { Timber.d("Session end cause saved") }
                    .doOnError {
                        Timber.d("Session end cause saved exception happened")
                        Timber.e(it)
                    }
                    .toSingleDefault(sessionJustEnded)
            }
            .flatMap { sessionJustEnded ->
                val sessionId = sessionJustEnded.id ?: throw IllegalStateException()
                onSessionEndedRelay.accept(sessionId)
                scoreRepository.getAllLiveScoresInSession(sessionId).map { scores ->
                    Pair(scores, sessionJustEnded)
                }.onErrorReturn {
                    Pair(emptyList(), sessionJustEnded)
                }
            }
            .flatMapCompletable { liveScoresEntity ->
                val liveScoresInSession = liveScoresEntity.first
                val sessionEntity = liveScoresEntity.second
                Completable.create { emitter ->
                    try {
                        val liveScores = liveScoresInSession.map { scoreEntity -> scoreEntity.value }
                        Timber.d("Supplying all session scores to score provider")
                        SleepSessionScoreProvider.getSessionSummary(liveScores.map { it.toFloat() }, sessionEntity.id ?: throw IllegalStateException())
                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            }
            .andThen(
                programEntityDao.findLatestProgram()
                    .flatMapCompletable { program ->
                        scoreRepository.getProgramSessionScores(program.id ?: throw IllegalStateException())
                            .doOnSuccess { scores ->
                                SleepSessionScoreProvider.getProgramSummary(scores.map { it.toFloat() }, program.id ?: throw IllegalStateException())
                            }
                            .ignoreElement()
                    }
            )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onComplete = {
                    Timber.d("Session ended successfully")
                },
                onError = { e ->
                    Timber.e(e, "Could not end session with session score generation")
                }
            )
            .addTo(disposables)
    }

    fun cancelCurrentSession(cause: SessionTerminatedCause?) {
        sessionEntityDao.findSessionInProgress()
            .flatMapCompletable { sessionInProgress ->
                sessionEntityDao.delete(sessionInProgress)
                    .doOnComplete {
                        Timber.d("Session deleted")
                        sessionInProgress.id?.let { sessionInProgressId ->
                            onSessionCanceledRelay.accept(sessionInProgressId)
                        }
                    }
            }.doOnComplete {
                sessionTerminatedRepository.saveTerminatedCause(
                    sessionId = null,
                    night = null,
                    sessionTerminatedCause = cause,
                    recorded = false
                )
                    .doOnComplete { Timber.d("Session end cause saved") }
                    .subscribe()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onComplete = {
                    Timber.d("Session ended successfully")
                },
                onError = { e ->
                    Timber.e(e, "Could cancel session")
                }
            )
            .addTo(disposables)
    }
}