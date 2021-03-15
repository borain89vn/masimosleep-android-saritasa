package com.mymasimo.masimosleep.ui.program_completed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreProvider
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.room.entity.ProgramEntity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ProgramCompletedViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val scoreRepository: SleepScoreRepository,
    schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _enableFullReportButton = MutableLiveData(false)
    val enableFullReportButton: LiveData<Boolean>
        get() = _enableFullReportButton

    var program: ProgramEntity? = null

    init {
        programRepository.getLatestProgram()
            .flatMapCompletable { program ->
                this.program = program
                scoreRepository.getProgramSessionScores(program.id ?: throw IllegalStateException())
                    .doOnSuccess { scores ->
                        SleepSessionScoreProvider.getProgramSummary(scores.map { it.toFloat() }, program.id ?: throw IllegalStateException())
                        SleepSessionScoreProvider.getSleepImprovement(scores.map { it.toFloat() }, program.id ?: throw IllegalStateException())
                    }
                    .flatMapCompletable {
                        programRepository.endProgram(program.id ?: throw IllegalStateException())
                    }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                { _enableFullReportButton.value = true },
                {
                    Timber.e(it)
                    _enableFullReportButton.value = true
                }
            )
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
