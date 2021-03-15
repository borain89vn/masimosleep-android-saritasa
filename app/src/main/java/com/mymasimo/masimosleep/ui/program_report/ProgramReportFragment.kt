package com.mymasimo.masimosleep.ui.program_report

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentProgramReportBinding
import com.mymasimo.masimosleep.ui.night_report.sleep_pattern.SleepPatternFragment
import com.mymasimo.masimosleep.ui.program_report.avg_sleep_quality.AverageSleepQualityFragment
import com.mymasimo.masimosleep.ui.program_report.end_early.EndProgramEarlyFragment
import com.mymasimo.masimosleep.ui.program_report.events.ProgramEventsFragment
import com.mymasimo.masimosleep.ui.program_report.nightly_scores.NightlyScoresFragment
import com.mymasimo.masimosleep.ui.program_report.outcome.ProgramOutcomeFragment
import com.mymasimo.masimosleep.ui.program_report.recommendations.ProgramRecommendationsFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val MONTH_DAY_PATTERN = "MMM dd"

private const val MONTH_DAY_YEAR_PATTERN = "MMM dd, yyyy"

class ProgramReportFragment : Fragment(R.layout.fragment_program_report) {

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramReportViewModel by viewModels { vmFactory }

    val args: ProgramReportFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentProgramReportBinding::bind)

    private var programEnded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        savedInstanceState?.let { state ->
            if (state.containsKey(KEY_PROGRAM_ENDED)) {
                programEnded = state.getBoolean(KEY_PROGRAM_ENDED)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_PROGRAM_ENDED, programEnded)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.backButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        vm.goToProgramCompleted.subscribe {
            programEnded = true
            findNavController().navigate(
                ProgramReportFragmentDirections.actionProgramReportFragmentToHomeFragment()
            )
        }.addTo(disposables)

        vm.programRange.observe(viewLifecycleOwner, { range ->
            updateTime(range.first, range.last)
        })

        vm.onCreated(args.programId)

        showReportConfiguration()
    }

    private fun updateTime(startMillis: Long, endMillis: Long) {
        val formatter = SimpleDateFormat(MONTH_DAY_PATTERN, Locale.getDefault())

        val timeCalendar = Calendar.getInstance().apply { timeInMillis = startMillis }

        val startFormatted = formatter.format(timeCalendar.time)

        timeCalendar.timeInMillis = endMillis

        formatter.applyPattern(MONTH_DAY_YEAR_PATTERN)

        val endTimeFormatted = formatter.format(timeCalendar.time)

        viewBinding.subtitleText.text = resources.getString(R.string.start_to_end_date, startFormatted, endTimeFormatted)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun showReportConfiguration() {
        removeAllFragments()

        addFragment(AverageSleepQualityFragment.newInstance(args.programId), AVG_SLEEP_QUALITY_FRAGMENT_TAG)
        if (args.isProgramCompleted || programEnded) {
            addFragment(ProgramOutcomeFragment.newInstance(args.programId), SLEEP_OUTCOME_TAG)
        }
        addFragment(NightlyScoresFragment.newInstance(args.programId), NIGHTLY_SCORES_TAG)
        addFragment(ProgramEventsFragment.newInstance(args.programId), EVENTS_FRAGMENT_TAG)
        addFragment(SleepPatternFragment.newInstanceWithProgramId(args.programId), SLEEP_PATTERN_FRAGMENT_TAG)
        addFragment(ProgramRecommendationsFragment.newInstance(args.programId), RECOMMENDATIONS_FRAGMENT_TAG)
        if (!args.isProgramCompleted && !programEnded) {
            addFragment(
                EndProgramEarlyFragment.newInstance().apply {
                    setOnClickListener {
                        findNavController().navigate(
                            ProgramReportFragmentDirections.actionProgramReportFragmentToEndProgramDialogFragment(
                                nightNumber = vm.sessionCount
                            )
                        )
                    }
                },
                END_PROGRAM_EARLY_TAG
            )
        }
    }

    private fun removeAllFragments() {
        ALL_FRAGMENT_TAGS.forEach { tag ->
            parentFragmentManager.findFragmentByTag(tag)?.let { fragment ->
                parentFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        parentFragmentManager.beginTransaction()
            .add(R.id.program_report_layout, fragment, tag)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val AVG_SLEEP_QUALITY_FRAGMENT_TAG = "AVG_SLEEP_QUALITY"
        private const val SLEEP_OUTCOME_TAG = "SLEEP_OUTCOME"
        private const val NIGHTLY_SCORES_TAG = "NIGHTLY_SCORES_TAG"
        private const val EVENTS_FRAGMENT_TAG = "EVENTS_FRAGMENT"
        private const val SLEEP_PATTERN_FRAGMENT_TAG = "SLEEP_PATTERN_FRAGMENT"
        private const val RECOMMENDATIONS_FRAGMENT_TAG = "RECOMMENDATIONS"
        private const val END_PROGRAM_EARLY_TAG = "END_PROGRAM"

        private const val KEY_PROGRAM_ENDED = "PROGRAM_ENDED_KEY"

        private val ALL_FRAGMENT_TAGS = listOf(
            AVG_SLEEP_QUALITY_FRAGMENT_TAG,
            SLEEP_OUTCOME_TAG,
            NIGHTLY_SCORES_TAG,
            EVENTS_FRAGMENT_TAG,
            SLEEP_PATTERN_FRAGMENT_TAG,
            RECOMMENDATIONS_FRAGMENT_TAG,
            END_PROGRAM_EARLY_TAG
        )
    }

}