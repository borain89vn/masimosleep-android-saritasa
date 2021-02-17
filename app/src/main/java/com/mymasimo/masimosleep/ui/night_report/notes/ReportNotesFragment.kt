package com.mymasimo.masimosleep.ui.night_report.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import kotlinx.android.synthetic.main.fragment_report_notes.*
import javax.inject.Inject

class ReportNotesFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportNotesViewModel by viewModels { vmFactory }

    private val adapter = ReportNotesAdapter(mutableListOf())

    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        vm.onCreated(sessionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        note_rv.layoutManager = LinearLayoutManager(context)
        note_rv.adapter = adapter

        updateUI(emptyList())
        vm.notes.observe(viewLifecycleOwner) { notes ->
            updateUI(notes)
        }

        add_note_button.setOnClickListener {
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToReportAddNoteFragment(
                    sessionId
                )
            )
        }
    }

    private fun updateUI(notes: List<Note>) {
        if (notes.isEmpty()) {
            note_rv.visibility = View.GONE
            no_notes_tray.visibility = View.VISIBLE
        } else {
            note_rv.visibility = View.VISIBLE
            no_notes_tray.visibility = View.GONE
        }

        adapter.setNotes(notes)
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportNotesFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}
