package com.mymasimo.masimosleep.ui.program_started

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentProgramStartedBinding
import javax.inject.Inject


class ProgramStartedFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: ProgramStartedViewModel by viewModels { vmFactory }
    private lateinit var bindings: FragmentProgramStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindings = FragmentProgramStartedBinding.inflate(inflater, container, false)
        return bindings.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.goToDashboardEnabled.observe(viewLifecycleOwner) { enabled ->
            bindings.submitButton.isEnabled = enabled
        }

        bindings.submitButton.setOnClickListener {
            goToDashboardScreen()
        }
    }

    private fun goToDashboardScreen() {
        requireView().findNavController().navigate(
            ProgramStartedFragmentDirections.actionProgramStartedFragmentToHomeFragment()
        )
    }
}