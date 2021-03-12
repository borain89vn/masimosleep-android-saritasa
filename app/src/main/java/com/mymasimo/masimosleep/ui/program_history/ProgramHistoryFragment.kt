package com.mymasimo.masimosleep.ui.program_history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentProgramHistoryBinding
import com.mymasimo.masimosleep.ui.program_history.util.Program
import javax.inject.Inject

class ProgramHistoryFragment : Fragment(R.layout.fragment_program_history) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramHistoryViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentProgramHistoryBinding::bind)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.programRv.layoutManager = LinearLayoutManager(context)
        viewBinding.programRv.adapter = adapter

        viewBinding.closeButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        vm.programs.observe(viewLifecycleOwner) { programs ->
            adapter.setPrograms(programs)
        }

        vm.onViewCreated()
    }
}