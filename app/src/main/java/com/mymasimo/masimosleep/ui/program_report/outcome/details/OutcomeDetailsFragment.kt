package com.mymasimo.masimosleep.ui.program_report.outcome.details

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentOutcomeDetailsBinding
import com.mymasimo.masimosleep.ui.program_report.outcome.SleepOutcome

class OutcomeDetailsFragment : Fragment(R.layout.fragment_outcome_details) {

    val args: OutcomeDetailsFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentOutcomeDetailsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    fun loadViewContent() {
        viewBinding.closeButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        updateUI()
    }

    private fun updateUI() {
        var titleID: Int = R.string.outcome_slight_title
        var bodyID: Int = R.string.outcome_slight_body
        var imageID: Int = R.drawable.outcome_slight_thumb

        when (args.outcome) {
            SleepOutcome.SLIGHT -> {
                titleID = R.string.outcome_slight_title
                bodyID = R.string.outcome_slight_body
                imageID = R.drawable.outcome_slight_image
            }

            SleepOutcome.TRENDING_DOWN -> {
                titleID = R.string.outcome_deterioration_title
                bodyID = R.string.outcome_deterioration_body
                imageID = R.drawable.outcome_deterioration_image
            }

            SleepOutcome.STABLE -> {
                titleID = R.string.outcome_inconclusive_title
                bodyID = R.string.outcome_inconclusive_body
                imageID = R.drawable.outcome_inconclusive_image
            }

            SleepOutcome.SIGNIFICANT -> {
                titleID = R.string.outcome_significant_title
                bodyID = R.string.outcome_significant_body
                imageID = R.drawable.outcome_significant_image
            }

        }

        viewBinding.outcomeTitle.text = resources.getString(titleID)
        viewBinding.outcomeBody.text = resources.getString(bodyID)
        viewBinding.outcomeImage.setImageDrawable(ResourcesCompat.getDrawable(resources, imageID, null))
    }
}