package com.mymasimo.masimosleep.ui.night_report.report_vitals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.databinding.FragmentReportVitalsBinding
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph.ReportLineGraphFragment
import com.mymasimo.masimosleep.ui.session.view_vitals.ChartIntervalType

class ReportVitalsFragment : Fragment(R.layout.fragment_report_vitals) {

    private var chartIntervalType: ChartIntervalType = ChartIntervalType.ALL
    private val args: ReportVitalsFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentReportVitalsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.backButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        viewBinding.allButton.isSelected = true
        chartIntervalType = ChartIntervalType.ALL
        updateUI()

        viewBinding.allButton.setOnClickListener {
            clearSelection()
            viewBinding.allButton.isSelected = true
            chartIntervalType = ChartIntervalType.ALL
            updateUI()

        }

        viewBinding.hourButton.setOnClickListener {
            clearSelection()
            viewBinding.hourButton.isSelected = true
            chartIntervalType = ChartIntervalType.HOUR
            updateUI()
        }

        viewBinding.minuteButton.setOnClickListener {
            clearSelection()
            viewBinding.minuteButton.isSelected = true
            chartIntervalType = ChartIntervalType.MINUTE
            updateUI()
        }

    }

    private fun clearSelection() {
        viewBinding.allButton.isSelected = false
        viewBinding.hourButton.isSelected = false
        viewBinding.minuteButton.isSelected = false
    }

    private fun updateUI() = when (chartIntervalType) {
        ChartIntervalType.MINUTE -> showLinearCharts(1)
        ChartIntervalType.ALL -> showLinearCharts(60 * 24)
        ChartIntervalType.HOUR -> showLinearCharts(60)
    }

    private fun showLinearCharts(scale: Int) {
        removeAllFragments()

        addFragment(
            ReportLineGraphFragment.newInstance(ReadingType.SP02, args.sessionId, scale),
            SPO2_LINE_FRAGMENT_TAG
        )
        addFragment(
            ReportLineGraphFragment.newInstance(ReadingType.PR, args.sessionId, scale),
            PR_LINE_FRAGMENT_TAG
        )
        addFragment(
            ReportLineGraphFragment.newInstance(ReadingType.RRP, args.sessionId, scale),
            RRP_LINE_FRAGMENT_TAG
        )
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
            .add(R.id.vitals_layout, fragment, tag)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val SPO2_LINE_FRAGMENT_TAG = "SPO2_LINE"
        private const val SPO2_INTERVAL_FRAGMENT_TAG = "SPO2_INTERVAL"
        private const val PR_LINE_FRAGMENT_TAG = "PR_LINE"
        private const val PR_INTERVAL_FRAGMENT_TAG = "PR_INTERVAL"
        private const val RRP_LINE_FRAGMENT_TAG = "RRP_LINE"
        private const val RRP_INTERVAL_FRAGMENT_TAG = "RRP_INTERVAL"

        private val ALL_FRAGMENT_TAGS = listOf(
            SPO2_LINE_FRAGMENT_TAG,
            SPO2_INTERVAL_FRAGMENT_TAG,
            PR_LINE_FRAGMENT_TAG,
            PR_INTERVAL_FRAGMENT_TAG,
            RRP_LINE_FRAGMENT_TAG,
            RRP_INTERVAL_FRAGMENT_TAG
        )
    }
}