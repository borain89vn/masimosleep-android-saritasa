package com.mymasimo.masimosleep.ui.program_completed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProgramCompletedBinding
import javax.inject.Inject

class ProgramCompletedFragment : Fragment(R.layout.fragment_program_completed) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramCompletedViewModel by viewModels { vmFactory }
    private val args: ProgramCompletedFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentProgramCompletedBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.subTitleLabel.text = getString(R.string.program_complete_content, MasimoSleepPreferences.name)

        vm.enableFullReportButton.observe(viewLifecycleOwner) { enable ->
            viewBinding.submitButton.isEnabled = enable
        }

        viewBinding.submitButton.setOnClickListener {
            if (args.endedEarly) {
                findNavController().navigateUp()
            } else {
                vm.program?.id?.let {
                    findNavController().navigate(
                        ProgramCompletedFragmentDirections.actionProgramCompletedFragmentToProgramReportFragment(programId = it, isProgramCompleted = true)
                    )
                } ?: kotlin.run {
                    findNavController().navigateUp()
                }
            }
        }
    }

}