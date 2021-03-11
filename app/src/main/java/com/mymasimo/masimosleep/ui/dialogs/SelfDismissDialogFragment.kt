package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Dialogs that will dismiss themselves when session is no longer in progress
 */
abstract class SelfDismissDialogFragment(@LayoutRes contentLayoutId: Int) : DialogFragment(contentLayoutId) {

    @Inject lateinit var sleepSessionScoreManager: SleepSessionScoreManager
    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    @Inject lateinit var schedulerProvider: SchedulerProvider
    @Inject lateinit var disposables: CompositeDisposable

    private val vm: SelfDismissDialogFragmentViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        vm.sessionInProgress
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                    { isSessionInProgress ->
                        if (!isSessionInProgress) this.dialog?.dismiss()
                    },
                    {
                        it.printStackTrace()
                    })
            .addTo(disposables)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}