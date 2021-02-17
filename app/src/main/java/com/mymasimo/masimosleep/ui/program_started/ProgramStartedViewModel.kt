package com.mymasimo.masimosleep.ui.program_started

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ProgramStartedViewModel @Inject constructor(
    programRepository: ProgramRepository,
    schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _goToDashboardEnabled = MutableLiveData(false)
    val goToDashboardEnabled: LiveData<Boolean>
        get() = _goToDashboardEnabled

    init {
        programRepository.createProgram()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                _goToDashboardEnabled.value = true
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}