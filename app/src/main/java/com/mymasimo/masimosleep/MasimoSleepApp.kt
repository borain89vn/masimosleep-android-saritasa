package com.mymasimo.masimosleep

import android.app.Application
import androidx.lifecycle.*
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.DaggerSingletonComponent
import com.mymasimo.masimosleep.dagger.SingletonComponent
import com.mymasimo.masimosleep.dagger.modules.ContextModule
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.repository.ModelStore
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.service.serviceConnectBLE
import com.mymasimo.masimosleep.util.initializeNotificationChannels
import com.mymasimo.masimosleep.util.test.FakeEventGenerator
import com.mymasimo.masimosleep.util.test.FakeTicker
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class MasimoSleepApp : Application(), LifecycleObserver {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var disposables: CompositeDisposable
    @Inject
    lateinit var fakeTicker: FakeTicker
    @Inject
    lateinit var fakeEventGenerator: FakeEventGenerator
    @Inject
    lateinit var programRepository: ProgramRepository
    @Inject
    lateinit var sessionRepository: SessionRepository
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

        connectToSavedModuleIfExists()

        setUpFakeData()

        resumeSessionIfWasInProgress()
    }

    private fun connectToSavedModuleIfExists() {
        Timber.d("connectToSavedModuleIfExists")
        if (MasimoSleepPreferences.selectedModuleId <= 0) {
            Timber.d("No saved module in DB - no need to connect right now...")
            return
        }

        ModelStore.onStartUpModuleLoaded
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                val module = ModelStore.currentModule
                module?.let {
                    Timber.d("Module in DB loaded - starting service to connect to it")
                    serviceConnectBLE(this)
                }
            }
            .addTo(disposables)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() = synchronized(this) {
        Timber.d("onEnterForeground()")
        _foreground.postValue(true)

        serviceConnectBLE(this)

        if (MasimoSleepPreferences.selectedModuleId <= 0) {
            Timber.d("No saved module in DB - no need to connect right now...")
            return
        }
        ModelStore.loadModule(MasimoSleepPreferences.selectedModuleId)
        connectToSavedModuleIfExists()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() = synchronized(this) {
        Timber.d("onEnterBackground()")
        _foreground.postValue(false)
    }

    private fun plantTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // TODO: Setup crash reporting tree.
            Timber.plant(Timber.DebugTree())
        }
    }

    @Suppress("ConstantConditionIf")
    private fun setUpFakeData() {
        if (BuildConfig.FAKE_TICKS_ENABLED) {
            fakeTicker.startFakeTicking()
        }
        if (BuildConfig.FAKE_EVENTS_ENABLED) {
            fakeEventGenerator.generateRandomEvents()
        }
    }

    private fun resumeSessionIfWasInProgress() {
        sessionRepository.getSessionInProgress()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                { session -> sleepSessionScoreManager.resumeSession(session.nightNumber, session.startAt) },
                { Timber.d("No session to resume.") }
            )
            .addTo(disposables)
    }

    companion object {
        private var INSTANCE: MasimoSleepApp? = null

        @JvmStatic
        fun get(): MasimoSleepApp = INSTANCE!!
    }
}