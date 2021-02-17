package com.mymasimo.masimosleep.ui.night_report.report_events.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import kotlinx.android.synthetic.main.fragment_event_details.*
import javax.inject.Inject

class EventDetailsFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: EventDetailsViewModel by viewModels { vmFactory }

    private val args : EventDetailsFragmentArgs by navArgs()

    private val adapter = EventDetailsAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreated(args.sessionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        event_rv.layoutManager = LinearLayoutManager(context)
        event_rv.adapter = adapter

        vm.viewData.observe(viewLifecycleOwner) { viewData ->
            updateUI(viewData)
        }

        back_button.setOnClickListener {
            requireView().findNavController().navigateUp()
        }
    }

    private fun updateUI(viewData: EventDetailsViewModel.EventDetailViewData) {
        val totalEvents = viewData.totalEvents

        var plural = "s"
        if (totalEvents == 1) {
            plural = ""
        }
        event_text.text = "$totalEvents Event$plural occured"

        minor_event_text.text = viewData.minorEvents.toString()
        major_event_text.text = viewData.majorEvents.toString()

        updateList(viewData.events)
    }

    private fun updateList(events : List<EventDetailsViewModel.EventDetailViewData.EventSummary>) {
        adapter.setEvents(events)
    }

}