package com.mymasimo.masimosleep.ui.program_report.nightly_scores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.program_report.ProgramReportFragmentDirections
import kotlinx.android.synthetic.main.fragment_nightly_scores.*
import javax.inject.Inject


class NightlyScoresFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: NightlyScoresViewModel by viewModels { vmFactory }

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreate(programId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nightly_scores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buildUI(emptyList())

        vm.scores.observe(viewLifecycleOwner) { scores ->
            buildUI(scores)
        }
    }

    private fun buildUI(scores: List<NightlyScoreItem>) {
        row1_layout.removeAllViews()
        row2_layout.removeAllViews()
        for (i in 1..NUM_OF_NIGHTS) {
            val index = i - 1
            var score : Int = -1
            var type : NightScoreButtonState = NightScoreButtonState.FUTURE

            var layout = row1_layout
            if (i > 5) {
                layout = row2_layout
            }

            if (index < scores.size) {
                score = (scores[index].score * 100).toInt()
                type = NightScoreButtonState.PAST
            }

            val button = NightScoreButtonView(requireContext(),i,score,type)

            if (index < scores.size) {
                button.setOnButtonClickListener {
                    val sessionId : Long = scores[index].sessionId
                    requireView().findNavController().navigate(
                        ProgramReportFragmentDirections.actionProgramReportFragmentToNightReportFragment(
                            sessionId = sessionId,
                            nightNumber = scores[index].nightNumber
                        )
                    )
                }
            }
            layout.addView(button)
        }
    }

    companion object {
        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = NightlyScoresFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }

}
