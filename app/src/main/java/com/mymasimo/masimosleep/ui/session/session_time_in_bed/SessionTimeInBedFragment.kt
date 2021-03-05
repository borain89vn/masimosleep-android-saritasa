package com.mymasimo.masimosleep.ui.session.session_time_in_bed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSessionTimeInBedBinding
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_session_time_in_bed.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SessionTimeInBedFragment : Fragment() {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var disposables: CompositeDisposable

    // Epoch timestamp of when the session started.
    private var startTimeMillis: Long = 0

    private lateinit var bindings: FragmentSessionTimeInBedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        startTimeMillis = requireArguments().getLong(KEY_SESSION_START_AT)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindings = FragmentSessionTimeInBedBinding.inflate(inflater, container, false)
        return bindings.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Observable.interval(0, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                updateTime()
            }
            .addTo(disposables)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun updateTime() {
        val nowSeconds = Calendar.getInstance().timeInMillis / 1000
        val elapsedTimeSeconds = nowSeconds - (startTimeMillis / 1000)

        val hour = elapsedTimeSeconds.toInt() / 3600
        val minute = (elapsedTimeSeconds.toInt() % 3600) / 60

        var elapsedString = hour.toString() + "h " + minute.toString() + "m"

        if (hour == 0) {
            elapsedString = minute.toString() + "m"
        }

        time_in_bed_text.text = elapsedString

        val formatter = SimpleDateFormat("MMM d, hh:mm aa", Locale.getDefault())
        val startTimeCalendar = Calendar.getInstance().apply {
            timeInMillis = startTimeMillis
        }

        sleep_time_text.text = formatter.format(startTimeCalendar.time)
    }


    companion object {
        private const val REFRESH_INTERVAL_SECONDS = 30L

        private const val KEY_SESSION_START_AT = "SESSION_START_AT"

        fun newInstance(sessionStartAt: Long) = SessionTimeInBedFragment().apply {
            arguments = bundleOf(KEY_SESSION_START_AT to sessionStartAt)
        }
    }
}