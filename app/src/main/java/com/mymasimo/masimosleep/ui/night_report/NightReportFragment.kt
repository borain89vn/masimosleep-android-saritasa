package com.mymasimo.masimosleep.ui.night_report

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.databinding.FragmentNightReportBinding
import com.mymasimo.masimosleep.ui.night_report.notes.ReportNotesFragment
import com.mymasimo.masimosleep.ui.night_report.recommendations.RecommendationsFragment
import com.mymasimo.masimosleep.ui.night_report.report_bed_time.ReportTimeInBedFragment
import com.mymasimo.masimosleep.ui.night_report.report_events.ReportEventsFragment
import com.mymasimo.masimosleep.ui.night_report.report_sleep_quality.ReportSleepQualityFragment
import com.mymasimo.masimosleep.ui.night_report.report_sleep_trend.ReportSleepTrendFragment
import com.mymasimo.masimosleep.ui.night_report.report_view_vitals.ReportViewVitalsFragment
import com.mymasimo.masimosleep.ui.night_report.sleep_pattern.SleepPatternFragment

class NightReportFragment : Fragment(R.layout.fragment_night_report) {

    private val args: NightReportFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentNightReportBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.subtitleText.text = getString(R.string.night_label, args.nightNumber, NUM_OF_NIGHTS)

        viewBinding.backButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        showReportConfiguration()
    }

    private fun showReportConfiguration() {
        removeAllFragments()

        addFragment(ReportSleepQualityFragment.newInstance(args.sessionId), SLEEP_QUALITY_FRAGMENT_TAG)
        addFragment(ReportTimeInBedFragment.newInstance(args.sessionId), TIME_IN_BED_FRAGMENT_TAG)
        addFragment(ReportSleepTrendFragment.newInstance(args.sessionId), SLEEP_TREND_FRAGMENT_TAG)
        addFragment(ReportEventsFragment.newInstance(args.sessionId), EVENTS_FRAGMENT_TAG)
        addFragment(SleepPatternFragment.newInstanceWithSessionId(args.sessionId), SLEEP_PATTERN_FRAGMENT_TAG)
        addFragment(RecommendationsFragment.newInstance(args.sessionId), RECOMMENDATIONS_FRAGMENT_TAG)
        addFragment(createViewVitalsFragment(args.sessionId), VIEW_VITALS_FRAGMENT_TAG)
        // Add button "Download CSV"
        addFragment(ReportNotesFragment.newInstance(args.sessionId), NOTES_FRAGMENT_TAG)
    }

    private fun createViewVitalsFragment(sessionId: Long): ReportViewVitalsFragment {
        return ReportViewVitalsFragment.newInstance().apply {
            setOnClickListener {
                requireView().findNavController().navigate(
                    NightReportFragmentDirections.actionNightReportFragmentToReportVitalsFragment(
                        sessionId
                    )
                )
            }
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
            .add(R.id.night_report_layout, fragment, tag)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val SLEEP_QUALITY_FRAGMENT_TAG = "SLEEP_QUALITY"
        private const val TIME_IN_BED_FRAGMENT_TAG = "TIME_IN_BED_FRAGMENT"
        private const val SLEEP_TREND_FRAGMENT_TAG = "SLEEP_TREND"
        private const val EVENTS_FRAGMENT_TAG = "EVENTS_FRAGMENT"
        private const val SLEEP_PATTERN_FRAGMENT_TAG = "SLEEP_PATTERN_FRAGMENT"
        private const val RECOMMENDATIONS_FRAGMENT_TAG = "RECOMMENDATIONS"
        private const val VIEW_VITALS_FRAGMENT_TAG = "VIEW_VITALS"
        private const val NOTES_FRAGMENT_TAG = "NOTES_FRAGMENT"

        private val ALL_FRAGMENT_TAGS = listOf(
            SLEEP_QUALITY_FRAGMENT_TAG,
            TIME_IN_BED_FRAGMENT_TAG,
            SLEEP_TREND_FRAGMENT_TAG,
            EVENTS_FRAGMENT_TAG,
            RECOMMENDATIONS_FRAGMENT_TAG,
            VIEW_VITALS_FRAGMENT_TAG,
            NOTES_FRAGMENT_TAG,
        )
    }
}
