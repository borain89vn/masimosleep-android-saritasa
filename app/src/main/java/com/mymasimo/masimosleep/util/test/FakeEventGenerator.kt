package com.mymasimo.masimosleep.util.test

import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

class FakeEventGenerator @Inject constructor(
    private val sleepEventRepository: SleepEventRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) {
    fun generateRandomEvents() {
        var start = System.currentTimeMillis()

        Observable.interval(5, TimeUnit.SECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.io())
            .subscribe {
                val end = System.currentTimeMillis()
                val randBoolean = Random.nextBoolean()
                newEvent(start, end, if (randBoolean) SleepEventType.MILD else SleepEventType.SEVERE)
                start = end
            }
            .addTo(disposables)
    }

    private fun newEvent(start: Long, end: Long, type: SleepEventType) {
        sleepEventRepository.saveSleepEvent(start, end, type)
    }
}
