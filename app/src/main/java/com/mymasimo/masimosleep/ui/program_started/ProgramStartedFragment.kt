package com.mymasimo.masimosleep.ui.program_started

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentProgramStartedBinding
import javax.inject.Inject


class ProgramStartedFragment : Fragment(R.layout.fragment_program_started) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: ProgramStartedViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentProgramStartedBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.goToDashboardEnabled.observe(viewLifecycleOwner) { enabled ->
            viewBinding.submitButton.isEnabled = enabled
        }

        viewBinding.submitButton.setOnClickListener {
            goToDashboardScreen()
        }
    }

    private fun goToDashboardScreen() {
        requireView().findNavController().navigate(
            ProgramStartedFragmentDirections.actionProgramStartedFragmentToHomeFragment()
        )
    }
}