package com.mymasimo.masimosleep.ui.night_report

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.repository.RawParameterReadingRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.databinding.FragmentNightReportBinding
import com.mymasimo.masimosleep.service.RawParameterReadingCsvExport
import com.mymasimo.masimosleep.ui.night_report.notes.ReportNotesFragment
import com.mymasimo.masimosleep.ui.night_report.recommendations.RecommendationsFragment
import com.mymasimo.masimosleep.ui.night_report.report_bed_time.ReportTimeInBedFragment
import com.mymasimo.masimosleep.ui.night_report.report_events.ReportEventsFragment
import com.mymasimo.masimosleep.ui.night_report.report_export_measurements.ReportExportMeasurementsFragment
import com.mymasimo.masimosleep.ui.night_report.report_sleep_quality.ReportSleepQualityFragment
import com.mymasimo.masimosleep.ui.night_report.report_sleep_trend.ReportSleepTrendFragment
import com.mymasimo.masimosleep.ui.night_report.report_view_vitals.ReportViewVitalsFragment
import com.mymasimo.masimosleep.ui.night_report.sleep_pattern.SleepPatternFragment
import timber.log.Timber
import javax.inject.Inject

class NightReportFragment : Fragment(R.layout.fragment_night_report) {
    @Inject
    lateinit var rawParameterReadingRepository: RawParameterReadingRepository
    @Inject
    lateinit var sessionRepository: SessionRepository
    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val args: NightReportFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentNightReportBinding::bind)

    private var sessionId: Long = -1
    private var nightNumber: Int = -1
    private var isShowToolBar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        isShowToolBar = requireArguments().getBoolean(KEY_SHOW_TOOL_BAR, true)
        if (isShowToolBar) {
            sessionId = args.sessionId
            nightNumber = args.nightNumber
        } else {
            sessionId = requireArguments().getLong(KEY_SESSION_ID)
            nightNumber = requireArguments().getInt(KEY_NIGHT_NUMBER)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isShowToolBar) {
            viewBinding.subtitleText.text =
                getString(R.string.night_label, args.nightNumber, NUM_OF_NIGHTS)

            viewBinding.backButton.setOnClickListener {
                requireView().findNavController().navigateUp()
            }
        } else {
            viewBinding.backButton.visibility = View.GONE
            viewBinding.titleLabel.visibility = View.GONE
            viewBinding.subtitleText.visibility = View.GONE
            viewBinding.topDiv.visibility = View.GONE
        }

        showReportConfiguration()
    }

    private fun showReportConfiguration() {
        removeAllFragments()

        addFragment(ReportSleepQualityFragment.newInstance(sessionId), SLEEP_QUALITY_FRAGMENT_TAG)
        addFragment(ReportTimeInBedFragment.newInstance(sessionId), TIME_IN_BED_FRAGMENT_TAG)
        addFragment(ReportSleepTrendFragment.newInstance(sessionId), SLEEP_TREND_FRAGMENT_TAG)
        addFragment(ReportEventsFragment.newInstance(sessionId), EVENTS_FRAGMENT_TAG)
        addFragment(SleepPatternFragment.newInstanceWithSessionId(sessionId), SLEEP_PATTERN_FRAGMENT_TAG)
        addFragment(RecommendationsFragment.newInstance(sessionId), RECOMMENDATIONS_FRAGMENT_TAG)
        addFragment(createViewVitalsFragment(sessionId), VIEW_VITALS_FRAGMENT_TAG)
        addFragment(createReportExportMeasurementsFragment(sessionId), EXPORT_MEASUREMENTS_FRAGMENT_TAG)
        addFragment(ReportNotesFragment.newInstance(sessionId), NOTES_FRAGMENT_TAG)
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

    private fun createReportExportMeasurementsFragment(sessionId: Long): ReportExportMeasurementsFragment {
        return ReportExportMeasurementsFragment.newInstance().apply {
            setOnClickListener {
                exportMeasurements(sessionId)
            }
        }
    }

    /**
     * Export raw sensor reding data for the current session into CSV file.
     */
    private fun exportMeasurements(sessionId: Long) {
        Toast.makeText(context!!, R.string.export_starting, 5000).show()

        val raw = sessionRepository
            .getSessionById(sessionId)
            .flatMap { session ->
                val endAt = session.endAt ?: System.currentTimeMillis()
                return@flatMap rawParameterReadingRepository
                    .getRawReadingCsvData(session.startAt, endAt, session.nightNumber)
                    .map { data ->
                        Triple(session.startAt, endAt, data)
                    }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { (startAt, endAt, data) ->
                if (data.isNullOrEmpty()) {
                    Toast.makeText(context!!, R.string.export_no_data, 5000).show()
                } else {
                    val resultUri = RawParameterReadingCsvExport.exportToDownloads(context!!, startAt, endAt, data)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        setDataAndType(resultUri, RawParameterReadingCsvExport.CSV_MIME_TYPE)
                        putExtra(Intent.EXTRA_STREAM, resultUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    Timber.d(resultUri.toString())
                    startActivity(Intent.createChooser(intent, "Open export result"))
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
        private const val EXPORT_MEASUREMENTS_FRAGMENT_TAG = "EXPORT_MEASUREMENTS"
        private const val KEY_SESSION_ID = "SESSION_ID"
        private const val KEY_NIGHT_NUMBER = "NIGHT_NUMBER"
        private const val KEY_SHOW_TOOL_BAR = "HIDE_TOOL_BAR"
        private val ALL_FRAGMENT_TAGS = listOf(
            SLEEP_QUALITY_FRAGMENT_TAG,
            TIME_IN_BED_FRAGMENT_TAG,
            SLEEP_TREND_FRAGMENT_TAG,
            EVENTS_FRAGMENT_TAG,
            RECOMMENDATIONS_FRAGMENT_TAG,
            VIEW_VITALS_FRAGMENT_TAG,
            NOTES_FRAGMENT_TAG,
            EXPORT_MEASUREMENTS_FRAGMENT_TAG,
        )

        fun newInstance(sessionId: Long, nightNumber: Int): NightReportFragment {
            return NightReportFragment().apply {
                arguments = bundleOf(
                    KEY_SESSION_ID to sessionId,
                    KEY_NIGHT_NUMBER to nightNumber,
                    KEY_SHOW_TOOL_BAR to false
                )
            }
        }

    }
}
