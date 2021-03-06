package com.mymasimo.masimosleep.ui.program_report.outcome

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentProgramOutcomeBinding
import com.mymasimo.masimosleep.ui.program_report.ProgramReportFragmentDirections
import javax.inject.Inject

class ProgramOutcomeFragment : Fragment(R.layout.fragment_program_outcome) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramOutcomeViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentProgramOutcomeBinding::bind)

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.outcomeValue.observe(viewLifecycleOwner) {
            updateOutcome(it)
        }
    }

    private fun updateOutcome(outcomeValue: Double) {
        val outcome = SleepOutcome.fromValue(outcomeValue)
        updateUI(outcome)
    }

    private fun updateUI(outcome: SleepOutcome) {

        var titleID: Int = R.string.outcome_slight_title
        var bodyID: Int = R.string.outcome_slight_body
        var imageID: Int = R.drawable.outcome_slight_thumb
        var bgColorID: Int = R.color.outcome_slight

        when (outcome) {

            SleepOutcome.SLIGHT -> {
                titleID = R.string.outcome_slight_title
                bodyID = R.string.outcome_slight_body
                imageID = R.drawable.outcome_slight_thumb
                bgColorID = R.color.outcome_slight
            }

            SleepOutcome.TRENDING_DOWN -> {
                titleID = R.string.outcome_deterioration_title
                bodyID = R.string.outcome_deterioration_body
                imageID = R.drawable.outcome_deterioration_thumb
                bgColorID = R.color.outcome_deterioration
            }

            SleepOutcome.STABLE -> {
                titleID = R.string.outcome_inconclusive_title
                bodyID = R.string.outcome_inconclusive_body
                imageID = R.drawable.outcome_inconclusive_thumb
                bgColorID = R.color.outcome_inconclusive
            }

            SleepOutcome.SIGNIFICANT -> {
                titleID = R.string.outcome_significant_title
                bodyID = R.string.outcome_significant_body
                imageID = R.drawable.outcome_significant_thumb
                bgColorID = R.color.outcome_significant
            }

        }

        viewBinding.outcomeTitleText.text = resources.getString(titleID)
        viewBinding.outcomeDescText.text = resources.getString(bodyID)
        viewBinding.thumbIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, imageID, null))
        viewBinding.outcomeTray.backgroundTintList = resources.getColorStateList(bgColorID, null)

        viewBinding.outcomeButton.setOnClickListener {
            requireView().findNavController().navigate(ProgramReportFragmentDirections.actionProgramReportFragmentToOutcomeDetailsFragment(outcome))
        }
    }

    companion object {
        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = ProgramOutcomeFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }
}