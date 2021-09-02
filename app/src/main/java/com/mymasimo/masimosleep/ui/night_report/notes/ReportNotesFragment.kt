package com.mymasimo.masimosleep.ui.night_report.notes

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportNotesBinding
import com.mymasimo.masimosleep.ui.home.ShareHomeEventViewModel
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class ReportNotesFragment : Fragment(R.layout.fragment_report_notes) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportNotesViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportNotesBinding::bind)
    private val homeEventVM: ShareHomeEventViewModel by activityViewModels()

    private val adapter = ReportNotesAdapter(mutableListOf())

    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        vm.onCreated(sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.noteRv.layoutManager = LinearLayoutManager(context)
        viewBinding.noteRv.adapter = adapter

        updateUI(emptyList())
        vm.notes.observe(viewLifecycleOwner) { notes ->
            updateUI(notes)
        }
        receiveSharedEvent()

        viewBinding.addNoteButton.setOnClickListener {
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToReportAddNoteFragment(
                    sessionId
                )
            )
        }
    }

    private fun receiveSharedEvent() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            homeEventVM.shareEvent.collect {  it->
                vm.onCreated(it.sessionId)
            }
        }
    }

    private fun updateUI(notes: List<Note>) {
        if (notes.isEmpty()) {
            viewBinding.noteRv.visibility = View.GONE
            viewBinding.noNotesTray.visibility = View.VISIBLE
        } else {
            viewBinding.noteRv.visibility = View.VISIBLE
            viewBinding.noNotesTray.visibility = View.GONE
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
