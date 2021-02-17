package com.mymasimo.masimosleep.ui.night_report.notes.addnote

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionNoteRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class ReportAddNoteViewModel @Inject constructor(
    private val sessionNoteRepository: SessionNoteRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _onNoteSaved = PublishRelay.create<Unit>()
    val onNoteSaved: Observable<Unit>
        get() = _onNoteSaved

    fun onAddButtonClick(sessionId: Long, note: String) {
        sessionNoteRepository.saveNote(sessionId, note)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onComplete = {
                    Timber.d("Note $note saved to DB under session $sessionId")
                    _onNoteSaved.accept(Unit)
                },
                onError = { e ->
                    Timber.e(e, "Error saving note to DB")
                }
            )
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}