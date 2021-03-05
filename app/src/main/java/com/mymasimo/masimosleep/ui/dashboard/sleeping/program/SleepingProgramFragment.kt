package com.mymasimo.masimosleep.ui.dashboard.sleeping.program

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSleepingProgramBinding
import com.mymasimo.masimosleep.ui.dashboard.sleeping.SleepSessionViewModel
import javax.inject.Inject

class SleepingProgramFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: SleepSessionViewModel by activityViewModels { vmFactory }
    private lateinit var bindings: FragmentSleepingProgramBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindings = FragmentSleepingProgramBinding.inflate(inflater, container, false)
        return bindings.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindings.startSleepSessionBtn.setOnClickListener {
            vm.onStartSessionClick()
            view.findNavController()
                .navigate(R.id.action_sleepingProgramFragment_to_nightSessionFragment)
        }
    }

    companion object {
        fun newInstance() = SleepingProgramFragment()
    }
}
