package com.mymasimo.masimosleep.ui.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val DELAY_IN_SECONDS = 2L

class SplashFragment : Fragment(R.layout.fragment_splash) {
    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        Observable.timer(DELAY_IN_SECONDS, TimeUnit.SECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToNavigationFragment())
            }.addTo(disposables)
    }

    override fun onPause() {
        disposables.clear()
        super.onPause()
    }
}