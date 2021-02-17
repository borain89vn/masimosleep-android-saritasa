package com.mymasimo.masimosleep.ui.program_report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreProvider
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.room.entity.ProgramEntity
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ProgramReportViewModel @Inject constructor(
        private val sessionRepository: SessionRepository,
        private val scoreRepository: SleepScoreRepository,
        dialogActionHandler: DialogActionHandler,
        private val schedulerProvider: SchedulerProvider,
        private val programRepository: ProgramRepository,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _programRange = MutableLiveData<Pair<Long, Long>>()
    val programRange: LiveData<Pair<Long, Long>>
        get() = _programRange

    private val _goToProgramCompleted = PublishRelay.create<Unit>()
    val goToProgramCompleted: Observable<Unit>
        get() = _goToProgramCompleted

    var sessionCount: Int = 0

    init {
        dialogActionHandler.actions
            .filter { it is DialogActionHandler.Action.EndProgramConfirmationClicked }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                endCurrentProgram()
            }
            .addTo(disposables)

    }

    private fun endCurrentProgram() {
        programRepository.getCurrentProgram()
            .flatMapCompletable { program ->
                scoreRepository.getProgramSessionScores(program.id ?: throw IllegalStateException())
                    .doOnSuccess { scores ->
                        SleepSessionScoreProvider.getProgramSummary(scores.map { it.toFloat() }, program.id ?: throw IllegalStateException())
                        SleepSessionScoreProvider.getSleepImprovement(scores.map { it.toFloat() }, program.id ?: throw IllegalStateException())
                    }
                    .flatMapCompletable {
                        programRepository.endProgram(
                                program.id ?: throw IllegalStateException()
                        )

                    }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({
                           _goToProgramCompleted.accept(Unit)
                       },
                       {
                           it.printStackTrace()
                       })
            .addTo(disposables)
    }

    private fun loadProgramSessionCount(programId: Long) {
        sessionRepository.countAllSessionsInProgram(programId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({
                           sessionCount = it

                       }, {
                           it.printStackTrace()
                       })
            .addTo(disposables)
    }

    fun onCreated(programId: Long) {
        programRepository.getProgram(programId)
            .zipWith(sessionRepository.getLatestEndedSession(programId),
                     BiFunction<ProgramEntity, SessionEntity, Pair<Long, Long>> { program, session ->
                         session.endAt?.let { sessionEndTime -> program.startDate to sessionEndTime } ?: -1L to -1L
                     })
            .flatMap {
                if (it.first == -1L) Single.error(Throwable("Must have a valid session end time"))
                else Single.just(it)
            }.subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({
                           _programRange.value = it
                       }, {
                           Timber.e(it, "Error while loading program $programId")

                       })
            .addTo(disposables)

        loadProgramSessionCount(programId)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
