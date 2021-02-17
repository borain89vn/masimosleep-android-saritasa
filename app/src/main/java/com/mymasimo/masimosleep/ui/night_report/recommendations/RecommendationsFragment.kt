package com.mymasimo.masimosleep.ui.night_report.recommendations

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
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import com.mymasimo.masimosleep.ui.night_report.recommendations.util.Recommendation
import kotlinx.android.synthetic.main.fragment_recommendations.*
import javax.inject.Inject

class RecommendationsFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: RecommendationsViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreated(requireArguments().getLong(SESSION_ID_KEY))
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
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
        coffee_button.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.NO_CAFFEINE
                    )
            )
        }

        snore_button.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_SIDEWAYS
                    )
            )
        }

        beer_button.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.NO_ALCOHOL
                    )
            )
        }

        exercise_button.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.EXERCISE
                    )
            )
        }

        meds_button.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_AID
                    )
            )
        }

        more_sleep_button.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.SLEEP_HOURS
                    )
            )
        }

        maintain_button.setOnClickListener {
            requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToRecommendationDetailFragment(
                            Recommendation.MAINTAIN_HEALTHY_LIFESTYLE
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
        private const val SESSION_ID_KEY = "SESSION_ID"

        fun newInstance(sessionId: Long): RecommendationsFragment {
            return RecommendationsFragment().apply {
                arguments = bundleOf(SESSION_ID_KEY to sessionId)
            }
        }
    }
}