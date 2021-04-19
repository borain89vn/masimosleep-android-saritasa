package com.mymasimo.masimosleep

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.DaggerSingletonComponent
import com.mymasimo.masimosleep.dagger.SingletonComponent
import com.mymasimo.masimosleep.dagger.modules.ContextModule
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SensorRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.service.serviceConnectBLE
import com.mymasimo.masimosleep.util.initializeNotificationChannels
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MasimoSleepApp : Application(), LifecycleObserver {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var programRepository: ProgramRepository

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
        sessionRepository.getSessionInProgress()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onSuccess = { session -> sleepSessionScoreManager.resumeSession(session.nightNumber, session.startAt) },
                onComplete = { Timber.d("No session to resume.") },
                onError = { Timber.e(it) }
            )
            .addTo(disposables)
    }

    private suspend fun connectToSavedSensorIfExists() {
        val sensorId = sensorRepository.getSelectedSensorId()
        val sensor = sensorRepository.loadSensor(sensorId).firstOrNull()
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