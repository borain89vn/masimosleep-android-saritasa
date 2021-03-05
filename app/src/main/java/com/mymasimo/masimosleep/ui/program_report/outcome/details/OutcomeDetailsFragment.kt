package com.mymasimo.masimosleep.ui.program_report.outcome.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.ui.program_report.outcome.SleepOutcome
import kotlinx.android.synthetic.main.fragment_outcome_details.*


class OutcomeDetailsFragment : Fragment() {

    val args: OutcomeDetailsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_outcome_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    fun loadViewContent() {
        close_button.setOnClickListener {
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

        outcome_title.text = resources.getString(titleID)
        outcome_body.text = resources.getString(bodyID)
        outcome_image.setImageDrawable(resources.getDrawable(imageID, null))
    }
}