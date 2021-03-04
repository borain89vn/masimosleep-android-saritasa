package com.mymasimo.masimosleep.ui.dashboard.sleeping.nightsession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentNightSessionBinding
import com.mymasimo.masimosleep.ui.dashboard.sleeping.SleepSessionViewModel
import javax.inject.Inject

class NightSessionFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: SleepSessionViewModel by activityViewModels { vmFactory }
    private lateinit var bindings: FragmentNightSessionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindings = FragmentNightSessionBinding.inflate(inflater, container, false)
        return bindings.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindings.endSleepSessionBtn.setOnClickListener {
            vm.onEndSessionClick()
            view.findNavController()
                .navigate(R.id.action_nightSessionFragment_to_sleepingProgramFragment)
        }

        vm.liveScore.observe(viewLifecycleOwner) { liveScore ->
            bindings.liveScoreView.text = getString(R.string.live_score, liveScore)
        }
    }

    companion object {
        fun newInstance() = NightSessionFragment()
    }
}
