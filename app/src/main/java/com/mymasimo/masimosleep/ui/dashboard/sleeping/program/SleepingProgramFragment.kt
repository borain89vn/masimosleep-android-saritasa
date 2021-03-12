package com.mymasimo.masimosleep.ui.dashboard.sleeping.program

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSleepingProgramBinding
import com.mymasimo.masimosleep.ui.dashboard.sleeping.SleepSessionViewModel
import javax.inject.Inject

class SleepingProgramFragment : Fragment(R.layout.fragment_sleeping_program) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: SleepSessionViewModel by activityViewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentSleepingProgramBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.startSleepSessionBtn.setOnClickListener {
            vm.onStartSessionClick()
            view.findNavController()
                .navigate(R.id.action_sleepingProgramFragment_to_nightSessionFragment)
        }
    }

    companion object {
        fun newInstance() = SleepingProgramFragment()
    }
}
