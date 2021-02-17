package com.mymasimo.masimosleep.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mymasimo.masimosleep.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

private const val DELAY_IN_SECONDS = 2L

class SplashFragment : Fragment() {
    private var disposable: Disposable?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onResume() {
        super.onResume()

        disposable = Observable.timer(DELAY_IN_SECONDS, TimeUnit.SECONDS)
            .subscribe {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToNavigationFragment())
            }
    }

    override fun onPause() {
        super.onPause()

        disposable?.dispose()
    }
}