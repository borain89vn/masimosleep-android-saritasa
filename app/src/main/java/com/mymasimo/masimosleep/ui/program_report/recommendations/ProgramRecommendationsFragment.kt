package com.mymasimo.masimosleep.ui.program_report.recommendations

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentProgramRecommendationsBinding
import com.mymasimo.masimosleep.ui.night_report.recommendations.util.Recommendation
import com.mymasimo.masimosleep.ui.program_report.ProgramReportFragmentDirections
import javax.inject.Inject

class ProgramRecommendationsFragment : Fragment(R.layout.fragment_program_recommendations) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramRecommendationsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentProgramRecommendationsBinding::bind)

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()

        vm.recommendations.observe(viewLifecycleOwner) { recommendations ->
            updateUI(recommendations)
        }
    }

    private fun setupButtons() {
        viewBinding.coffeeTray.setOnClickListener {
            requireView().findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                    Recommendation.NO_CAFFEINE
                )
            )
        }

        viewBinding.snoreTray.setOnClickListener {
            requireView().findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                    Recommendation.SLEEP_SIDEWAYS
                )
            )
        }

        viewBinding.beerTray.setOnClickListener {
            requireView().findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                    Recommendation.NO_ALCOHOL
                )
            )
        }

        viewBinding.exerciseTray.setOnClickListener {
            requireView().findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                    Recommendation.EXERCISE
                )
            )
        }

        viewBinding.medsTray.setOnClickListener {
            requireView().findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                    Recommendation.SLEEP_AID
                )
            )
        }

        viewBinding.moreSleepTray.setOnClickListener {
            requireView().findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                    Recommendation.SLEEP_HOURS
                )
            )
        }

        viewBinding.maintainTray.setOnClickListener {
            requireView().findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToRecommendationDetailFragment(
                    Recommendation.MAINTAIN_HEALTHY_LIFESTYLE
                )
            )
        }
    }

    fun updateUI(recommendations: Set<Recommendation>) {
        viewBinding.coffeeTray.isVisible = recommendations.any { it == Recommendation.NO_CAFFEINE }
        viewBinding.snoreTray.isVisible = recommendations.any { it == Recommendation.SLEEP_SIDEWAYS }
        viewBinding.beerTray.isVisible = recommendations.any { it == Recommendation.NO_ALCOHOL }
        viewBinding.exerciseTray.isVisible = recommendations.any { it == Recommendation.EXERCISE }
        viewBinding.medsTray.isVisible = recommendations.any { it == Recommendation.SLEEP_AID }
        viewBinding.moreSleepTray.isVisible = recommendations.any { it == Recommendation.SLEEP_HOURS }
        viewBinding.maintainTray.isVisible = recommendations.any { it == Recommendation.MAINTAIN_HEALTHY_LIFESTYLE }
    }

    companion object {
        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = ProgramRecommendationsFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }

}