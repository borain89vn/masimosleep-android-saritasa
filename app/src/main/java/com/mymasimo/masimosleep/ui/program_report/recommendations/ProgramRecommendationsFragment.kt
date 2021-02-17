package com.mymasimo.masimosleep.ui.program_report.recommendations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.night_report.recommendations.util.Recommendation
import com.mymasimo.masimosleep.ui.program_report.ProgramReportFragmentDirections
import kotlinx.android.synthetic.main.fragment_program_recommendations.*
import javax.inject.Inject

class ProgramRecommendationsFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramRecommendationsViewModel by viewModels { vmFactory }

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_program_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()

        vm.recommendations.observe(viewLifecycleOwner) { recommendations ->
            updateUI(recommendations)
        }
    }

    private fun setupButtons() {
        coffee_button.setOnClickListener {
            requireView().findNavController().navigate(
                    ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                            Recommendation.NO_CAFFEINE
                    )
            )
        }

        snore_button.setOnClickListener {
            requireView().findNavController().navigate(
                    ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_SIDEWAYS
                    )
            )
        }

        beer_button.setOnClickListener {
            requireView().findNavController().navigate(
                    ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                            Recommendation.NO_ALCOHOL
                    )
            )
        }

        exercise_button.setOnClickListener {
            requireView().findNavController().navigate(
                    ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                            Recommendation.EXERCISE
                    )
            )
        }

        meds_button.setOnClickListener {
            requireView().findNavController().navigate(
                    ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_AID
                    )
            )
        }

        maintain_button.setOnClickListener {
            requireView().findNavController().navigate(
                    ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_HOURS
                    )
            )
        }
    }

    fun updateUI(recommendations: Set<Recommendation>) {
        coffee_tray.isVisible = recommendations.any { it == Recommendation.NO_CAFFEINE }
        snore_tray.isVisible = recommendations.any { it == Recommendation.SLEEP_SIDEWAYS }
        beer_tray.isVisible = recommendations.any { it == Recommendation.NO_ALCOHOL }
        exercise_tray.isVisible = recommendations.any { it == Recommendation.EXERCISE }
        meds_tray.isVisible = recommendations.any { it == Recommendation.SLEEP_AID }
        more_sleep_tray.isVisible = recommendations.any { it == Recommendation.SLEEP_HOURS }
        maintain_tray.isVisible = recommendations.any { it == Recommendation.MAINTAIN_HEALTHY_LIFESTYLE }
    }

    companion object {

        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = ProgramRecommendationsFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }

}