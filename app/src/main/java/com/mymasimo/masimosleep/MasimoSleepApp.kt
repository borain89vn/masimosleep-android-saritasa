package com.mymasimo.masimosleep

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mymasimo.masimosleep.base.dispatchers.CoroutineDispatchers
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.DaggerSingletonComponent
import com.mymasimo.masimosleep.dagger.SingletonComponent
import com.mymasimo.masimosleep.dagger.modules.ContextModule
import com.mymasimo.masimosleep.data.repository.SensorRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.service.serviceConnectBLE
import com.mymasimo.masimosleep.util.initializeNotificationChannels
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MasimoSleepApp : Application(), LifecycleObserver {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var dispatchers: CoroutineDispatchers

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var sensorRepository: SensorRepository

    @Inject
    lateinit var sleepSessionScoreManager: SleepSessionScoreManager

    lateinit var component: SingletonComponent

    private val _foreground = MutableLiveData(false)
    val foreground: LiveData<Boolean>
        get() = _foreground

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        initializeNotificationChannels(this)

        plantTimberTree()

        component = DaggerSingletonComponent
            .builder()
            .contextModule(ContextModule(this, this))
            .build().apply {
                inject(this@MasimoSleepApp)
            }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() = synchronized(this) {
        Timber.d("onEnterForeground()")
        _foreground.postValue(true)

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            connectToSavedSensorIfExists()
        }

        resumeSessionIfWasInProgress()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() = synchronized(this) {
        Timber.d("onEnterBackground()")
        _foreground.postValue(false)
    }

    private fun plantTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun resumeSessionIfWasInProgress() {
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            val session: SessionEntity? = withContext(dispatchers.io()) { sessionRepository.getSessionInProgress().blockingGet() }
            if (session == null) {
                Timber.d("No session to resume.")
            } else {
                sleepSessionScoreManager.resumeSession(session.nightNumber, session.startAt)
            }
        }
    }

    private suspend fun connectToSavedSensorIfExists() {
        val sensor = sensorRepository.getSelectedSensor()
        if (sensor == null) {
            Timber.d("No saved sensor - no need to connect right now...")
        } else {
            Timber.d("Sensor in DB loaded - starting service to connect to it")
            serviceConnectBLE(this)
        }
    }

    companion object {
        private var INSTANCE: MasimoSleepApp? = null

        @JvmStatic
        fun get(): MasimoSleepApp = INSTANCE!!
    }
}