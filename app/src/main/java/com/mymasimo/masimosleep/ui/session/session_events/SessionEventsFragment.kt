package com.mymasimo.masimosleep.ui.session.session_events

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentSessionEventsBinding
import com.mymasimo.masimosleep.ui.night_report.NightReportFragment
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import com.mymasimo.masimosleep.ui.session.session_events.util.SleepEventsViewData
import io.reactivex.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SessionEventsFragment : Fragment(R.layout.fragment_session_events) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: SessionSleepEventsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentSessionEventsBinding::bind)

    companion object {
        private const val START_TIME_KEY = "START_TIME"
        private const val KEY_REQUEST_CLICK = "CLICK"
        private const val KEY_RESULT_OPEN_VITAL_DETAIL = "OPEN_VITAL"


        fun newInstance(startAt: Long) = SessionEventsFragment().apply {
            arguments = bundleOf(
                START_TIME_KEY to startAt
            )
        }
    }

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            startTime = getLong(START_TIME_KEY)
        }

        vm.onCreate(startTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.noEventsText.text = getString(R.string.sleep_events_empty, MasimoSleepPreferences.name)
        noEventsConfiguration()

        vm.sleepEvents.observe(viewLifecycleOwner) { sleepEventData ->
            updateUI(sleepEventData)
        }

        viewBinding.arrowIcon.setOnClickListener {
            sendClickEventToParent(true)
        }

        viewBinding.viewVitalTitle.setOnClickListener {
            sendClickEventToParent(true)
        }

        viewBinding.infoButton.setOnClickListener {
            sendClickEventToParent(false)
        }

    }
    private fun sendClickEventToParent(isOpenVitalDetail: Boolean) {
        parentFragment?.setFragmentResult(
            KEY_REQUEST_CLICK, bundleOf(KEY_RESULT_OPEN_VITAL_DETAIL to isOpenVitalDetail)
        )
    }

    private fun noEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.VISIBLE
        viewBinding.eventTray.visibility = View.GONE
    }

    private fun receivedEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.GONE
        viewBinding.eventTray.visibility = View.VISIBLE
    }

    private fun updateUI(sleepEventData: SleepEventsViewData) {
        val totalEvents = sleepEventData.totalEvents

        if (totalEvents == 0) {
            noEventsConfiguration()
        } else {
            receivedEventsConfiguration()
        }

        viewBinding.eventText.text = resources.getQuantityString(R.plurals.events_occurred, totalEvents, totalEvents)

        viewBinding.minorEventText.text = sleepEventData.minorEvents.toString()
        viewBinding.majorEventText.text = sleepEventData.majorEvents.toString()

    }

}