package com.mymasimo.masimosleep.ui.night_report.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionNoteRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ReportNotesViewModel @Inject constructor(
    private val notesRepository: SessionNoteRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>>
        get() = _notes

    fun onCreated(sessionId: Long) {
        notesRepository.getAllSessionNotesUpdates(sessionId)
            .map { notes -> notes.map { Note(it.createdAt, it.note) } }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { notes ->
                _notes.value = notes
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
