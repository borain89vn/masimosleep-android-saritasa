package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.data.room.dao.SessionNoteEntityDao
import com.mymasimo.masimosleep.data.room.entity.SessionNoteEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionNoteRepository @Inject constructor(
    private val sessionNoteEntityDao: SessionNoteEntityDao
) {
    fun saveNote(sessionId: Long, note: String): Completable {
        return sessionNoteEntityDao.insert(
            SessionNoteEntity(
                sessionId = sessionId,
                note = note
            )
        )
    }

    fun getAllSessionNotes(sessionId: Long): Single<List<SessionNoteEntity>> {
        return sessionNoteEntityDao.findAllBySessionId(sessionId)
    }

    fun getAllSessionNotesUpdates(sessionId: Long): Observable<List<SessionNoteEntity>> {
        return sessionNoteEntityDao.findAllBySessionIdUpdates(sessionId)
    }
}