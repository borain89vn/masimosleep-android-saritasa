package com.mymasimo.masimosleep.ui.session.addnote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionNoteRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class AddNoteViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionNoteRepository: SessionNoteRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _savingInProgress = MutableLiveData(false)
    val savingInProgress: LiveData<Boolean>
        get() = _savingInProgress

    private val _onNoteSaved = PublishRelay.create<Unit>()
    val onNoteSaved: Observable<Unit>
        get() = _onNoteSaved

    fun onAddButtonClick(note: String) {
        sessionRepository.getSessionInProgressId()
            .flatMapCompletable { sessionId ->
                sessionNoteRepository.saveNote(sessionId, note)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onComplete = {
                    Timber.d("Note $note saved to DB under current session")
                    _savingInProgress.value = false
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