package com.mymasimo.masimosleep.ui.program_report.outcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreProvider
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ProgramOutcomeViewModel @Inject constructor(
        private val programRepository: ProgramRepository,
        private val scoreRepository: SleepScoreRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _outcome = MutableLiveData<Double>()
    val outcomeValue: LiveData<Double>
        get() = _outcome

    fun onCreated(programId: Long) {
        programRepository.getProgramFlowable(programId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { program ->
                _outcome.value = program.outcome
            }
            .addTo(disposables)

        programRepository.getProgram(programId)
            .flatMap { program ->
                scoreRepository.getProgramSessionScores(program.id ?: throw IllegalStateException())
                    .doOnSuccess { scores ->
                        SleepSessionScoreProvider.getSleepImprovement(scores.map { it.toFloat() }, program.id ?: throw IllegalStateException())
                    }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ scores ->
                           Timber.d("Trying to calculate outcome again from ${scores.joinToString()}")
                       },
                       {
                           it.printStackTrace()
                       })
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
