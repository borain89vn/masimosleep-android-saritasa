package com.mymasimo.masimosleep.ui.program_report.events

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProgramEventsBinding
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import javax.inject.Inject


class ProgramEventsFragment : Fragment(R.layout.fragment_program_events) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramEventsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentProgramEventsBinding::bind)

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.eventsViewData.observe(viewLifecycleOwner) { viewData ->
            updateUI(viewData)
        }


    }

    private fun noEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.VISIBLE
        viewBinding.eventTray.visibility = View.GONE

    }

    private fun receivedEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.GONE
        viewBinding.eventTray.visibility = View.VISIBLE
        viewBinding.noEventsText.visibility = View.GONE
    }

    private fun updateUI(eventsData: ProgramEventsViewModel.ProgramEventsViewData) {
        val totalEvents = eventsData.totalEvents

        if (totalEvents == 0) {
            noEventsConfiguration()
        } else {
            receivedEventsConfiguration()
        }
        viewBinding.noEventsText.text = getString(R.string.day_events_empty, MasimoSleepPreferences.name)
        viewBinding.eventText.text = resources.getQuantityString(R.plurals.events_occurred, totalEvents, totalEvents)

        viewBinding.minorEventText.text = eventsData.minorEvents.toString()
        viewBinding.majorEventText.text = eventsData.majorEvents.toString()

    }





    companion object {

        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = ProgramEventsFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }
}