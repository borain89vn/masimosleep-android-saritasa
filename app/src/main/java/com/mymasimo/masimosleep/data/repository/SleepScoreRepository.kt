package com.mymasimo.masimosleep.data.repository

import com.jakewharton.rxrelay2.PublishRelay
import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreProvider
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.ScoreEntityDao
import com.mymasimo.masimosleep.data.room.dao.SessionEntityDao
import com.mymasimo.masimosleep.data.room.entity.ScoreEntity
import com.mymasimo.masimosleep.data.room.entity.ScoreType
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepScoreRepository @Inject constructor(
        private val scoreEntityDao: ScoreEntityDao,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable,
        private val sessionEntityDao: SessionEntityDao
) {

    private val onLiveScoreSavedRelay = PublishRelay.create<Unit>()
    val onLiveScoreSaved: Observable<Unit>
        get() = onLiveScoreSavedRelay

    fun saveLiveScore(value: Double) {
        sessionEntityDao.findSessionInProgressId()
            .flatMapCompletable { sessionId ->
                scoreEntityDao.insert(
                        ScoreEntity(
                                sessionId = sessionId,
                                value = value,
                                type = ScoreType.LIVE,
                                createdAt = Calendar.getInstance().timeInMillis
                        ))
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                    onComplete = {
                        onLiveScoreSavedRelay.accept(Unit)
                    },
                    onError = { e ->
                        Timber.d(e, "Error saving live score")
                    }
            )
            .addTo(disposables)
    }

    fun saveSessionScore(value: Double, sessionId: Long) {
        sessionEntityDao.findSessionById(sessionId)
            .flatMapCompletable { session ->
                scoreEntityDao.insert(
                        ScoreEntity(
                                sessionId = session.id ?: throw IllegalStateException(),
                                value = value,
                                type = ScoreType.SESSION,
                                createdAt = Calendar.getInstance().timeInMillis
                        ))
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                    onComplete = {
                        Timber.d("Session score: $value saved")
                    },
                    onError = { e ->
                        Timber.d(e, "Error saving session score")
                    }
            )
            .addTo(disposables)
    }

    fun recentLiveScoreUpdates(startAt: Long): Flowable<Double> {
        return scoreEntityDao.recentLiveScoreUpdates(startAt).map { score -> score.value }
    }

    fun getAllLiveScoresFrom(startAt: Long): Single<List<ScoreEntity>> {
        return scoreEntityDao.findAllLiveScoresFrom(startAt)
    }

    fun getAllLiveScoresInSession(sessionId: Long): Single<List<ScoreEntity>> {
        return sessionEntityDao.findSessionById(sessionId)
            .flatMap { session ->
                val endAt = session.endAt
                    ?: return@flatMap Single.error<List<ScoreEntity>>(
                            IllegalStateException("Session doesn't have an endAt")
                    )
                return@flatMap scoreEntityDao.findLiveScoresBetweenTimestamps(
                        session.startAt, endAt)
            }
    }

    fun getProgramSessionScores(programId: Long): Single<List<Double>> {
        return sessionEntityDao.findAllByProgramId(programId)
            .flatMap { sessions ->
                if (sessions.isEmpty()) {
                    return@flatMap Single.just(emptyList<Double>())
                }
                return@flatMap Single.zip(
                        sessions.map { getSessionScore(it.id ?: throw IllegalStateException()) },
                        Function<Array<Any>, List<Double>> { data ->
                            val sessionScores = mutableListOf<ScoreEntity>()
                            data.forEach {
                                sessionScores.add(it as ScoreEntity)
                            }
                            return@Function sessionScores.map { it.value }
                        }
                )
            }
    }

    fun getSessionScore(sessionId: Long): Single<ScoreEntity> {
        return scoreEntityDao.findSessionScoreBySessionId(sessionId)
            .doOnError {
                getAllLiveScoresInSession(sessionId)
                    .onErrorReturn { emptyList() }
                    .subscribe({ liveScoresInSession ->
                                   val liveScores =
                                       liveScoresInSession.map { scoreEntity -> scoreEntity.value }
                                   Timber.d("Supplying all session scores to score provider")
                                   SleepSessionScoreProvider.getSessionSummary(liveScores.map { it.toFloat() }, sessionId)
                               }, { it.printStackTrace() })
            }
            //In case session score didn't get computed correctly
            .retryWhen { e -> e.delay(200, TimeUnit.MILLISECONDS) }
    }
}