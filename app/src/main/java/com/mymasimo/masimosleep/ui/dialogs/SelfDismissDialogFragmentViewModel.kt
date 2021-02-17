package com.mymasimo.masimosleep.ui.dialogs

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SelfDismissDialogFragmentViewModel @Inject constructor(
        schedulerProvider: SchedulerProvider,
        sleepSessionScoreManager: SleepSessionScoreManager,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _sessionInProgress = BehaviorRelay.create<Boolean>()
    val sessionInProgress: Observable<Boolean>
        get() = _sessionInProgress

    init {
        sleepSessionScoreManager.isSessionInProgressRelay
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { isSessionInProgress ->
                _sessionInProgress.accept(isSessionInProgress)
            }.addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}