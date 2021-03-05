package com.mymasimo.masimosleep.ui.waking.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.room.entity.SurveyAnswer
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestion
import com.mymasimo.masimosleep.databinding.FragmentSurveyBinding
import kotlinx.android.synthetic.main.fragment_survey.*
import javax.inject.Inject

class SurveyFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: SurveyViewModel by viewModels { vmFactory }
    private lateinit var bindings: FragmentSurveyBinding
    private val args: SurveyFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
        vm.onCreated(args.sessionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindings = FragmentSurveyBinding.inflate(inflater, container, false)
        return bindings.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindings.title.text = getString(R.string.greeting_title, MasimoSleepPreferences.name)
        bindings.subTitle.text = getString(R.string.night_completed_label, args.nightNumber, NUM_OF_NIGHTS)

        vm.enableButton.observe(viewLifecycleOwner) { action ->
            bindings.submitButton.setOnClickListener {
                vm.onSubmitClick()
                if (action.programEnded) {
                    goToProgramCompletedScreen()
                } else {
                    goToRemoveChipScreen()
                }
            }

            bindings.skipButton.setOnClickListener {
                vm.onSkipClicked()
                if (action.programEnded) {
                    goToProgramCompletedScreen()
                } else {
                    goToRemoveChipScreen()
                }
            }
        }

        survey_scroll_view.setOnScrollChangeListener { _, _, scrollY, _, _ ->
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
            swipe_up_icon?.visibility = View.INVISIBLE
            swipe_up_title?.text = getString(R.string.take_survey)

        }
    }

    private fun setupButtons() {

        coffee_no_button.setOnClickListener {
            coffee_no_button.isSelected = true
            coffee_yes_button.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.CAFFEINE_, SurveyAnswer.NO)
        }

        coffee_yes_button.setOnClickListener {
            coffee_no_button.isSelected = false
            coffee_yes_button.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.CAFFEINE_, SurveyAnswer.YES)
        }

        snoring_no_button.setOnClickListener {
            snoring_no_button.isSelected = true
            snoring_yes_button.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.SNORING, SurveyAnswer.NO)
        }

        snoring_yes_button.setOnClickListener {
            snoring_no_button.isSelected = false
            snoring_yes_button.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.SNORING, SurveyAnswer.YES)
        }

        beer_no_button.setOnClickListener {
            beer_no_button.isSelected = true
            beer_yes_button.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.ALCOHOL, SurveyAnswer.NO)
        }

        beer_yes_button.setOnClickListener {
            beer_no_button.isSelected = false
            beer_yes_button.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.ALCOHOL, SurveyAnswer.YES)
        }

        exercise_no_button.setOnClickListener {
            exercise_no_button.isSelected = true
            exercise_yes_button.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.EXERCISE, SurveyAnswer.NO)
        }

        exercise_yes_button.setOnClickListener {
            exercise_no_button.isSelected = false
            exercise_yes_button.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.EXERCISE, SurveyAnswer.YES)
        }

        meds_no_button.setOnClickListener {
            meds_no_button.isSelected = true
            meds_yes_button.isSelected = false
            vm.onQuestionAnswered(SurveyQuestion.SLEEP_DRUG, SurveyAnswer.NO)
        }

        meds_yes_button.setOnClickListener {
            meds_no_button.isSelected = false
            meds_yes_button.isSelected = true
            vm.onQuestionAnswered(SurveyQuestion.SLEEP_DRUG, SurveyAnswer.YES)
        }

    }
}