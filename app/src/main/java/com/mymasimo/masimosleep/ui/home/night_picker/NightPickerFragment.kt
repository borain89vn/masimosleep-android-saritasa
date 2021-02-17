package com.mymasimo.masimosleep.ui.home.night_picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentNightPickerBinding
import com.mymasimo.masimosleep.ui.home.HomeFragmentDirections
import com.mymasimo.masimosleep.ui.home.HomeViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_night_picker.*
import javax.inject.Inject

class NightPickerFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: HomeViewModel by activityViewModels { vmFactory }
    private lateinit var bindings: FragmentNightPickerBinding

    private var currentNight = 1
    private var selectedNight = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        bindings = FragmentNightPickerBinding.inflate(inflater, container, false)
        return bindings.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindings.startProgramButton.setOnClickListener {
            goToProgramStartedScreen()
        }

        vm.programState.observe(viewLifecycleOwner) { programState ->
            when (programState) {
                HomeViewModel.ProgramState.NoProgramInProgress  -> {
                    bindings.startProgramContainer.isVisible = true
                    bindings.nightsContainer.isVisible = false
                }
                is HomeViewModel.ProgramState.ProgramInProgress -> {
                    bindings.startProgramContainer.isVisible = false
                    bindings.nightsContainer.isVisible = true

                    currentNight = programState.currentNight
                    selectedNight = programState.selectedNight
                    updateNightsUi()
                }
            }
        }
    }

    private fun goToProgramStartedScreen() {
        requireView().findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToProgramStartedFragment()
        )
    }

    private fun updateNightsUi() {
        bindings.nightLabel.text = "Night $selectedNight of $NUM_OF_NIGHTS"
        removeAllNightItems()

        val c = selectedNight + 2
        var r = (c - 1) % NUM_NIGHT_BUTTONS_IN_SCREEN + 1
        val f = NUM_NIGHT_BUTTONS_IN_SCREEN - r
        if (r == 5) {
            r = 0
        }

        val totalPositions: Int = f + NUM_OF_NIGHTS + r

        var scrollToPage: Int = 0
        for (i in 1..totalPositions) {

            //add front and rear blanks
            var state: NightButtonState = NightButtonState.BLANK
            val night: Int = i - f

            if (night in 1..NUM_OF_NIGHTS) {
                if (night < currentNight) {
                    state = NightButtonState.PAST
                    if (night == selectedNight) {
                        state = NightButtonState.PAST_SELECTED
                        scrollToPage = i / NUM_NIGHT_BUTTONS_IN_SCREEN
                    }
                } else if (night == currentNight) {
                    state = NightButtonState.PRESENT
                    if (night == selectedNight) {
                        scrollToPage = i / NUM_NIGHT_BUTTONS_IN_SCREEN
                    }
                } else {
                    state = NightButtonState.FUTURE
                }
            }

            val nightButton = generateNightButton(night, state)
            nightButton.setOnButtonClickListener {
                this.onNightSelected(night)
            }
            night_layout.addView(nightButton)

        }

        val screenWidth = resources.displayMetrics.widthPixels
        val scrollOffset = scrollToPage * screenWidth

        bindings.nightScrollView.post {
            kotlin.run {
                nightScrollView?.scrollTo(scrollOffset, 0)
            }
        }
    }

    private fun onNightSelected(night: Int) {
        this.selectedNight = night
        updateNightsUi()
        vm.onNightSelected(night)
    }

    private fun generateNightButton(night: Int, state: NightButtonState): NightButtonView {
        return NightButtonView(requireContext(), night, state)
    }

    private fun removeAllNightItems() {
        if (night_layout.childCount > 0) {
            night_layout.removeAllViews()
        }
    }

    companion object {
        private const val NUM_NIGHT_BUTTONS_IN_SCREEN = 5
    }
}