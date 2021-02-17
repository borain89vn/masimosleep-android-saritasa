package com.mymasimo.masimosleep.ui.program_completed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import kotlinx.android.synthetic.main.fragment_program_completed.*
import javax.inject.Inject

class ProgramCompletedFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramCompletedViewModel by viewModels { vmFactory }
    private val args: ProgramCompletedFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_program_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sub_title_label.text = getString(R.string.program_complete_content, MasimoSleepPreferences.name)

        vm.enableFullReportButton.observe(viewLifecycleOwner) { enable ->
            submit_button.isEnabled = enable
        }

        submit_button.setOnClickListener {
            if (args.endedEarly) {
                findNavController().navigateUp()
            } else {
                vm.program?.id?.let {
                    findNavController().navigate(
                            ProgramCompletedFragmentDirections.actionProgramCompletedFragmentToProgramReportFragment(programId = it, isProgramCompleted = true))
                } ?: kotlin.run {
                    findNavController().navigateUp()
                }
            }
        }

    }

}