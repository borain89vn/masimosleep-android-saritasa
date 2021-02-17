package com.mymasimo.masimosleep.ui.program_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.program_history.util.Program
import kotlinx.android.synthetic.main.fragment_program_history.*
import javax.inject.Inject

class ProgramHistoryFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramHistoryViewModel by viewModels { vmFactory }

    private val adapter = ProgramHistoryAdapter(mutableListOf()) { program ->
        if (program.sessionCount != 0) {
            requireView().findNavController().navigate(
                    ProgramHistoryFragmentDirections.actionProgramHistoryFragmentToProgramReportFragment(
                            programId = program.id,
                            isProgramCompleted = program is Program.Past
                    )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_program_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        program_rv.layoutManager = LinearLayoutManager(context)
        program_rv.adapter = adapter

        close_button.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        vm.programs.observe(viewLifecycleOwner) { programs ->
            adapter.setPrograms(programs)
        }

        vm.onViewCreated()
    }
}