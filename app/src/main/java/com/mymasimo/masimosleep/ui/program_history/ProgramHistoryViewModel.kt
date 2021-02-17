package com.mymasimo.masimosleep.ui.program_history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.ui.program_history.util.Program
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ProgramHistoryViewModel @Inject constructor(
        private val programRepository: ProgramRepository,
        private val sessionRepository: SessionRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _programs = MutableLiveData<List<Program>>()
    val programs: LiveData<List<Program>>
        get() = _programs

    fun onViewCreated() {
        loadPrograms()
    }

    private fun loadPrograms() {
        programRepository.getAllPrograms()
            .toObservable()
            .flatMapIterable { programs ->
                programs
            }.flatMapSingle { program ->
                program.id?.let { programId ->
                    Single.just(program)
                        .flatMap {
                            sessionRepository.countAllSessionsInProgram(programId)
                        }.map { sessionCount ->
                            val isCurrent = program.endDate == null
                            if (isCurrent) {
                                Program.Current(
                                        id = programId,
                                        startAt = program.startDate,
                                        score = program.score,
                                        sessionCount = sessionCount
                                )
                            } else {
                                Program.Past(
                                        id = programId,
                                        startAt = program.startDate,
                                        endAt = program.endDate ?: throw IllegalStateException(),
                                        score = program.score,
                                        sessionCount = sessionCount
                                )
                            }
                        }
                } ?: Single.error(Throwable("Program ID must not be null"))
            }.toList()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { programs ->
                _programs.value = programs
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
