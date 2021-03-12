package com.mymasimo.masimosleep.ui.waking.survey

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.room.entity.SurveyAnswer
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestion
import com.mymasimo.masimosleep.databinding.FragmentSurveyBinding
import javax.inject.Inject

class SurveyFragment : Fragment(R.layout.fragment_survey) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: SurveyViewModel by viewModels { vmFactory }
    private val args: SurveyFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentSurveyBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
        vm.onCreated(args.sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.title.text = getString(R.string.greeting_title, MasimoSleepPreferences.name)
        viewBinding.subTitle.text = getString(R.string.night_completed_label, args.nightNumber, NUM_OF_NIGHTS)

        vm.enableButton.observe(viewLifecycleOwner) { action ->
            viewBinding.submitButton.setOnClickListener {
                vm.onSubmitClick()
                if (action.programEnded) {
                    goToProgramCompletedScreen()
                } else {
                    goToRemoveChipScreen()
                }
            }

            viewBinding.skipButton.setOnClickListener {
                vm.onSkipClicked()
                if (action.programEnded) {
                    goToProgramCompletedScreen()
                } else {
                    goToRemoveChipScreen()
                }
            }
        }

        viewBinding.surveyScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            updateUI(scrollY)
        }

        setupButtons()
    }

    private fun goToRemoveChipScreen() {
        requireView().findNavController().navigate(
            SurveyFragmentDirections.actionSurveyFragmentToRemoveChipFragment(
                sessionId = args.sessionId
            )
        )
    }

    private fun goToProgramCompletedScreen() {
        requireView().findNavController().navigate(
            SurveyFragmentDirections.actionSurveyFragmentToProgramCompletedFragment(
                endedEarly = false
            )
        )
    }

    fun updateUI(scrollY: Int) {
        if (scrollY > 500) {
            viewBinding.swipeUpIcon.visibility = View.INVISIBLE
            viewBinding.swipeUpTitle.text = getString(R.string.take_survey)

        }
    }

    private fun setupButtons() {

        viewBinding.coffeeNoButton.setOnClickListener {
            viewBinding.coffeeNoButton.isSelected = true
            viewBinding.coffeeYesButton.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.CAFFEINE_, SurveyAnswer.NO)
        }

        viewBinding.coffeeYesButton.setOnClickListener {
            viewBinding.coffeeNoButton.isSelected = false
            viewBinding.coffeeYesButton.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.CAFFEINE_, SurveyAnswer.YES)
        }

        viewBinding.snoringNoButton.setOnClickListener {
            viewBinding.snoringNoButton.isSelected = true
            viewBinding.snoringYesButton.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.SNORING, SurveyAnswer.NO)
        }

        viewBinding.snoringYesButton.setOnClickListener {
            viewBinding.snoringNoButton.isSelected = false
            viewBinding.snoringYesButton.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.SNORING, SurveyAnswer.YES)
        }

        viewBinding.beerNoButton.setOnClickListener {
            viewBinding.beerNoButton.isSelected = true
            viewBinding.beerYesButton.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.ALCOHOL, SurveyAnswer.NO)
        }

        viewBinding.beerYesButton.setOnClickListener {
            viewBinding.beerNoButton.isSelected = false
            viewBinding.beerYesButton.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.ALCOHOL, SurveyAnswer.YES)
        }

        viewBinding.exerciseNoButton.setOnClickListener {
            viewBinding.exerciseNoButton.isSelected = true
            viewBinding.exerciseYesButton.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.EXERCISE, SurveyAnswer.NO)
        }

        viewBinding.exerciseYesButton.setOnClickListener {
            viewBinding.exerciseNoButton.isSelected = false
            viewBinding.exerciseYesButton.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.EXERCISE, SurveyAnswer.YES)
        }

        viewBinding.medsNoButton.setOnClickListener {
            viewBinding.medsNoButton.isSelected = true
            viewBinding.medsYesButton.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.SLEEP_DRUG, SurveyAnswer.NO)
        }

        viewBinding.medsYesButton.setOnClickListener {
            viewBinding.medsNoButton.isSelected = false
            viewBinding.medsYesButton.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.SLEEP_DRUG, SurveyAnswer.YES)
        }

    }
}