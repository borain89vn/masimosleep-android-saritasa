package com.mymasimo.masimosleep.ui.night_report.recommendations

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
import com.mymasimo.masimosleep.databinding.FragmentRecommendationsBinding
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import com.mymasimo.masimosleep.ui.night_report.recommendations.util.Recommendation
import javax.inject.Inject

class RecommendationsFragment : Fragment(R.layout.fragment_recommendations) {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: RecommendationsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentRecommendationsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreated(requireArguments().getLong(SESSION_ID_KEY))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        updateUI(emptySet())

        vm.recommendations.observe(viewLifecycleOwner) { recommendations ->
            updateUI(recommendations)
        }
    }

    private fun setupButtons() {
        viewBinding.coffeeTray.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.NO_CAFFEINE
                    )
            )
        }

        viewBinding.snoreTray.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_SIDEWAYS
                    )
            )
        }

        viewBinding.beerTray.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.NO_ALCOHOL
                    )
            )
        }

        viewBinding.exerciseTray.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.EXERCISE
                    )
            )
        }

        viewBinding.medsTray.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_AID
                    )
            )
        }

        viewBinding.moreSleepTray.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_HOURS
                    )
            )
        }

        viewBinding.maintainTray.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
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
        private const val SESSION_ID_KEY = "SESSION_ID"

        fun newInstance(sessionId: Long): RecommendationsFragment {
            return RecommendationsFragment().apply {
                arguments = bundleOf(SESSION_ID_KEY to sessionId)
            }
        }
    }
}