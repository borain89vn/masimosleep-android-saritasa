package com.mymasimo.masimosleep.ui.session.session_minor_event_detail

import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SessionMinorEventsViewModel @Inject constructor(
    private val eventsRepository: SleepEventRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {


}