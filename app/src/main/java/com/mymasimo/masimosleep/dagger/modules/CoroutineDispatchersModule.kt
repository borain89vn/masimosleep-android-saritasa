package com.mymasimo.masimosleep.dagger.modules

import com.mymasimo.masimosleep.base.dispatchers.AppDispatchers
import com.mymasimo.masimosleep.base.dispatchers.CoroutineDispatchers
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CoroutineDispatchersModule {
    @Singleton
    @Provides
    fun provideDispatchers(): CoroutineDispatchers = AppDispatchers()
}