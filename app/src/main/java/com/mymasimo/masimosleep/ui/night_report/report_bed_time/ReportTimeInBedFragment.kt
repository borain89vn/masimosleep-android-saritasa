package com.mymasimo.masimosleep.ui.night_report.report_bed_time

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportTimeInBedBinding
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReportTimeInBedFragment : Fragment(R.layout.fragment_report_time_in_bed) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportTimeInBedViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportTimeInBedBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
        vm.onCreated(requireArguments().getLong(KEY_SESSION_ID))
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

        viewBinding.timeInBedText.text = elapsedString

        val formatter = SimpleDateFormat("MMM d, hh:mm aa", Locale.getDefault())
        val timeCalendar = Calendar.getInstance().apply {
            timeInMillis = startMillis
        }

        viewBinding.sleepTimeText.text = formatter.format(timeCalendar.time)

        timeCalendar.timeInMillis = endMillis
        viewBinding.wakeTimeText.text = formatter.format(timeCalendar.time)
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportTimeInBedFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}
