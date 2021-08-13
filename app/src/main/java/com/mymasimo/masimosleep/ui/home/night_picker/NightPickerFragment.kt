package com.mymasimo.masimosleep.ui.home.night_picker

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentNightPickerBinding
import com.mymasimo.masimosleep.ui.home.HomeFragmentDirections
import com.mymasimo.masimosleep.ui.home.HomeViewModel
import com.mymasimo.masimosleep.util.DateOfWeek
import javax.inject.Inject
import kotlin.collections.HashMap

class NightPickerFragment : Fragment(R.layout.fragment_night_picker) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: HomeViewModel by activityViewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentNightPickerBinding::bind)

    private var currentNight = 1
    private var selectedNight = 1
    private var localDates = HashMap<Int,DateOfWeek>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.startProgramButton.setOnClickListener {
            goToProgramStartedScreen()
        }

        vm.programState.observe(viewLifecycleOwner) { programState ->
            when (programState) {
                HomeViewModel.ProgramState.NoProgramInProgress -> {
                    viewBinding.startProgramContainer.isVisible = true
                    viewBinding.nightsContainer.isVisible = false
                }
                is HomeViewModel.ProgramState.ProgramInProgress -> {
                    viewBinding.startProgramContainer.isVisible = false
                    viewBinding.nightsContainer.isVisible = true

                    currentNight = programState.currentNight
                    selectedNight = programState.selectedNight
                    vm.initDatesOfWeek(programState)
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

        viewBinding.nightLabel.text = getString(R.string.night_label, selectedNight, NUM_OF_NIGHTS)

        val daysLeft = NUM_OF_NIGHTS - selectedNight
        viewBinding.nightLabel.text = getString(R.string.sleep_program_title)
        viewBinding.daysLeftLabel.text = resources.getQuantityString(R.plurals.dayLeft,daysLeft,daysLeft)
        vm.homeTitle.postValue(getString(R.string.night_label_btn,selectedNight))

        removeAllNightItems()

        val c = selectedNight + 2
        var r = (c - 1) % NUM_NIGHT_BUTTONS_IN_SCREEN + 1
        val f = NUM_NIGHT_BUTTONS_IN_SCREEN - r
        if (r == 5) {
            r = 0
        }

        val totalPositions: Int = f + NUM_OF_NIGHTS + r

        var scrollToPage = 0
        for (i in 1..totalPositions) {

            //add front and rear blanks
            var state: NightButtonState = NightButtonState.BLANK
            val night: Int = i - f
           var dateModel :DateOfWeek? =null

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
                dateModel = vm.datesOfWeek[night-1]
            }



            val nightButton = generateNightButton(night, state,dateModel)
            nightButton.setOnButtonClickListener {
                this.onNightSelected(night)
            }
            viewBinding.nightLayout.addView(nightButton)

        }

        val screenWidth = resources.displayMetrics.widthPixels
        val scrollOffset = scrollToPage * screenWidth

        viewBinding.nightScrollView.post {
            kotlin.run {
                viewBinding.nightScrollView.scrollTo(scrollOffset, 0)
            }
        }
    }

    private fun onNightSelected(night: Int) {
        this.selectedNight = night
        updateNightsUi()
        vm.onNightSelected(night)
    }

    private fun generateNightButton(night: Int, state: NightButtonState,dateModel: DateOfWeek?): NightButtonView {
        return NightButtonView(requireContext(), night, state,dateModel)
    }

    private fun removeAllNightItems() {
        if (viewBinding.nightLayout.childCount > 0) {
            viewBinding.nightLayout.removeAllViews()
        }
    }

    companion object {
        private const val NUM_NIGHT_BUTTONS_IN_SCREEN = 5
    }
}