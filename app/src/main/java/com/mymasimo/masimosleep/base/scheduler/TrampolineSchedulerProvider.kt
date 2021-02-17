package com.mymasimo.masimosleep.base.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class TrampolineSchedulerProvider : SchedulerProvider {
    override fun io(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun ui(): Scheduler {
        return Schedulers.trampoline()
    }
}