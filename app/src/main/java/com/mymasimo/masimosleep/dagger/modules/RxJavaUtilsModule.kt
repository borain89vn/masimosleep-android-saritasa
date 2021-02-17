package com.mymasimo.masimosleep.dagger.modules

import com.mymasimo.masimosleep.base.scheduler.AndroidSchedulerProvider
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Singleton

@Module
class RxJavaUtilsModule {

    @Provides
    fun disposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    @Singleton
    @Provides
    fun schedulerProvider(schedulerProvider: AndroidSchedulerProvider): SchedulerProvider {
        return schedulerProvider
    }
}