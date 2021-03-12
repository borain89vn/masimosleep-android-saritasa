package com.mymasimo.masimosleep.ui.night_report.report_events.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentEventDetailsBinding
import javax.inject.Inject

class EventDetailsFragment : Fragment(R.layout.fragment_event_details) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: EventDetailsViewModel by viewModels { vmFactory }

    private val args: EventDetailsFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentEventDetailsBinding::bind)

    private val adapter = EventDetailsAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreated(args.sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.eventRv.layoutManager = LinearLayoutManager(context)
        viewBinding.eventRv.adapter = adapter

        vm.viewData.observe(viewLifecycleOwner) { viewData ->
            updateUI(viewData)
        }

        viewBinding.backButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }
    }

    private fun updateUI(viewData: EventDetailsViewModel.EventDetailViewData) {
        val totalEvents = viewData.totalEvents
        viewBinding.eventText.text = resources.getQuantityString(R.plurals.events_occurred, totalEvents, totalEvents)

        viewBinding.minorEventText.text = viewData.minorEvents.toString()
        viewBinding.majorEventText.text = viewData.majorEvents.toString()

        updateList(viewData.events)
    }

    private fun updateList(events: List<EventDetailsViewModel.EventDetailViewData.EventSummary>) {
        adapter.setEvents(events)
    }
}