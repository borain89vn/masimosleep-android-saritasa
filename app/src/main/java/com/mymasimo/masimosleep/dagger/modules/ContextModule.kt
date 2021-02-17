package com.mymasimo.masimosleep.dagger.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ContextModule(
    private val appContext: Context,
    private val app: Application
) {
    @Provides
    fun appContext(): Context = appContext

    @Provides
    fun app(): Application = app
}
