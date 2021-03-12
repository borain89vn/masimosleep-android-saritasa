package com.mymasimo.masimosleep.ui.dashboard.sleeping.nightsession

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentNightSessionBinding
import com.mymasimo.masimosleep.ui.dashboard.sleeping.SleepSessionViewModel
import javax.inject.Inject

class NightSessionFragment : Fragment(R.layout.fragment_night_session) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: SleepSessionViewModel by activityViewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentNightSessionBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.endSleepSessionBtn.setOnClickListener {
            vm.onEndSessionClick()
            view.findNavController()
                .navigate(R.id.action_nightSessionFragment_to_sleepingProgramFragment)
        }

        vm.liveScore.observe(viewLifecycleOwner) { liveScore ->
            viewBinding.liveScoreView.text = getString(R.string.live_score, liveScore)
        }
    }

    companion object {
        fun newInstance() = NightSessionFragment()
    }
}
