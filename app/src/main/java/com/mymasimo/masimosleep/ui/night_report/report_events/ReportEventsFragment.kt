package com.mymasimo.masimosleep.ui.night_report.report_events

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentReportEventsBinding
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import com.mymasimo.masimosleep.ui.night_report.report_events.util.SleepEventsViewData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ReportEventsFragment : Fragment(R.layout.fragment_report_events) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportEventsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportEventsBinding::bind)

    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        vm.onCreated(sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.sleepEvents.observe(viewLifecycleOwner) { viewData ->
            updateUI(viewData)
        }

        viewBinding.arrowIcon.setOnClickListener {
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToEventDetailsFragment(
                    sessionId
                )
            )
        }
    }

    private fun noEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.VISIBLE
        viewBinding.eventTray.visibility = View.GONE
        viewBinding.noEventsText.visibility = View.VISIBLE
    }

    private fun receivedEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.GONE
        viewBinding.eventTray.visibility = View.VISIBLE
        viewBinding.noEventsText.visibility = View.GONE
    }

    private fun updateUI(sleepEventData: SleepEventsViewData) {
        val totalEvents = sleepEventData.totalEvents

        if (totalEvents == 0) {
            noEventsConfiguration()
        } else {
            receivedEventsConfiguration()
        }

        viewBinding.noEventsText.text = getString(R.string.day_events_empty, MasimoSleepPreferences.name)
        viewBinding.eventText.text = resources.getQuantityString(R.plurals.events_occurred, totalEvents, totalEvents)

        viewBinding.minorEventText.text = sleepEventData.minorEvents.toString()
        viewBinding.majorEventText.text = sleepEventData.majorEvents.toString()

    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportEventsFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}