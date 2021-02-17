package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.data.room.dao.SessionTerminatedEntityDao
import com.mymasimo.masimosleep.data.room.entity.SessionTerminatedEntity
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTerminatedRepository @Inject constructor(
        private val sessionTerminatedEntityDao: SessionTerminatedEntityDao
) {
    fun saveTerminatedCause(sessionId: Long?, night: Int?, sessionTerminatedCause: SessionTerminatedCause?, recorded: Boolean): Completable {
        return sessionTerminatedEntityDao.insert(
                SessionTerminatedEntity(
                        sessionId = sessionId,
                        night = night,
                        cause = sessionTerminatedCause,
                        handled = false,
                        recorded = recorded
                )
        )
    }

    fun findLatestTerminatedModelNotHandled(): Single<SessionTerminatedEntity> {
        return sessionTerminatedEntityDao.findLatestTerminatedModelNotHandled()
    }

    fun updateSessionTerminatedEntity(sessionTerminatedEntity: SessionTerminatedEntity): Completable {
        return sessionTerminatedEntityDao.update(sessionTerminatedEntity)
    }

    fun updatedHandledLatestTerminatedModel(): Completable {
        return sessionTerminatedEntityDao.findLatestTerminatedModelNotHandled()
            .flatMapCompletable { session ->
                val sessionTerminatedEntityUpdated = SessionTerminatedEntity(id = session.id,
                                                                             sessionId = session.id,
                                                                             night = session.night,
                                                                             cause = session.cause,
                                                                             recorded = session.recorded,
                                                                             handled = true)
                sessionTerminatedEntityDao.update(sessionTerminatedEntityUpdated)
            }
            .onErrorComplete()
    }

}