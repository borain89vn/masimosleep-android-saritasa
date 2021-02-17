package com.mymasimo.masimosleep.ui.remove_chip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class RemoveChipViewModel @Inject constructor(
        programRepository: ProgramRepository,
        sessionRepository: SessionRepository,
        schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _enableButton = MutableLiveData<ButtonAction>()
    val enableButton: LiveData<ButtonAction>
        get() = _enableButton

    init {
        programRepository.getLatestProgram()
            .flatMap { program ->
                sessionRepository.getAllSessionsByProgramId(
                        program.id ?: throw IllegalStateException())
                    .map { sessionsInProgram -> sessionsInProgram.size >= NUM_OF_NIGHTS }
            }
            .doOnError {
                Timber.d("All programs are ended")
            }
            .onErrorReturnItem(true)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ programEnded ->
                           _enableButton.value = ButtonAction(programEnded = programEnded)
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

    data class ButtonAction(
            val programEnded: Boolean
    )
}
