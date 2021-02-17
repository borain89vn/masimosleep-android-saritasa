package com.mymasimo.masimosleep.base.scheduler

import io.reactivex.Scheduler

interface SchedulerProvider {
    fun io(): Scheduler

    fun ui(): Scheduler
}