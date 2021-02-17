package com.mymasimo.masimosleep.ui.night_report.report_bed_time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import kotlinx.android.synthetic.main.fragment_report_time_in_bed.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReportTimeInBedFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportTimeInBedViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreated(requireArguments().getLong(KEY_SESSION_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_time_in_bed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.sleepRange.observe(viewLifecycleOwner) { sleepRange ->
            updateTime(sleepRange.first, sleepRange.second)
        }
    }

    private fun updateTime(startMillis: Long, endMillis: Long) {
        val elapsedTimeSeconds = (endMillis / 1000) - (startMillis / 1000)

        val hour = elapsedTimeSeconds.toInt() / 3600
        val minute = (elapsedTimeSeconds.toInt() % 3600) / 60

        var elapsedString = hour.toString() + "h " + minute.toString() + "m"

        if (hour == 0) {
            elapsedString = minute.toString() + "m"
        }

        time_in_bed_text.text = elapsedString

        val formatter = SimpleDateFormat("MMM d, hh:mm aa", Locale.getDefault())
        val timeCalendar = Calendar.getInstance().apply {
            timeInMillis = startMillis
        }

        sleep_time_text.text = formatter.format(timeCalendar.time)

        timeCalendar.timeInMillis = endMillis
        wake_time_text.text = formatter.format(timeCalendar.time)
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportTimeInBedFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}
