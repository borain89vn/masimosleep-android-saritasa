package com.mymasimo.masimosleep.ui.night_report.recommendations.details

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentRecommendationDetailBinding
import com.mymasimo.masimosleep.ui.night_report.recommendations.util.Recommendation

class RecommendationDetailFragment : Fragment(R.layout.fragment_recommendation_detail) {
    val args: RecommendationDetailFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentRecommendationDetailBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.closeButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        loadViewContent()
    }

    private fun loadViewContent() {
        var titleID: Int = R.string.reco_coffee_title
        var subtitleID: Int = R.string.reco_coffee_subtitle
        var bodyID: Int = R.string.reco_coffee_body
        var imageID: Int = R.drawable.reco_coffee_image

        when (args.recommendation) {
            Recommendation.NO_CAFFEINE -> {
                titleID = R.string.reco_coffee_title
                subtitleID = R.string.reco_coffee_subtitle
                bodyID = R.string.reco_coffee_body
                imageID = R.drawable.reco_coffee_image
            }
            Recommendation.SLEEP_SIDEWAYS -> {
                titleID = R.string.reco_snore_title
                subtitleID = R.string.reco_snore_subtitle
                bodyID = R.string.reco_snore_body
                imageID = R.drawable.reco_side_image
            }
            Recommendation.NO_ALCOHOL -> {
                titleID = R.string.reco_beer_title
                subtitleID = R.string.reco_beer_subtitle
                bodyID = R.string.reco_beer_body
                imageID = R.drawable.reco_beer_image
            }
            Recommendation.EXERCISE -> {
                titleID = R.string.reco_exercise_title
                subtitleID = R.string.reco_exercise_subtitle
                bodyID = R.string.reco_exercise_body
                imageID = R.drawable.reco_shoe_image
            }
            Recommendation.SLEEP_AID -> {
                titleID = R.string.reco_meds_title
                subtitleID = R.string.reco_meds_subtitle
                bodyID = R.string.reco_meds_body
                imageID = R.drawable.reco_pills_image
            }
            Recommendation.SLEEP_HOURS -> {
                titleID = R.string.reco_more_sleep_title
                subtitleID = R.string.reco_more_sleep_subtitle
                bodyID = R.string.reco_more_sleep_body
                imageID = R.drawable.reco_more_sleep_image
            }
            Recommendation.MAINTAIN_HEALTHY_LIFESTYLE -> {
                titleID = R.string.reco_maintain_title
                subtitleID = R.string.reco_maintain_subTitle
                bodyID = R.string.reco_maintain_body
                imageID = R.drawable.reco_maintain_image
            }
        }

        viewBinding.recTitle.text = resources.getString(titleID)
        viewBinding.recSubTitle.text = resources.getString(subtitleID)
        viewBinding.recBody.text = resources.getString(bodyID)
        viewBinding.recoImage.setImageDrawable(ResourcesCompat.getDrawable(resources, imageID, null))
    }
}
