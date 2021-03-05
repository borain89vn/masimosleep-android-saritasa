package com.mymasimo.masimosleep.ui.session.vitals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.ui.session.view_vitals.ChartIntervalType
import com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.LiveIntervalGraphFragment
import com.mymasimo.masimosleep.ui.session.vitals.live.linegraph.LiveLineGraphFragment
import kotlinx.android.synthetic.main.fragment_session_vitals.*

class SessionVitalsFragment : Fragment() {

    companion object {
        fun newInstance(): SessionVitalsFragment {
            return SessionVitalsFragment()
        }

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

    private var chartIntervalType: ChartIntervalType = ChartIntervalType.ALL
    val args: SessionVitalsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.sessionStart
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_session_vitals, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        back_button.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        all_button.isSelected = true
        chartIntervalType = ChartIntervalType.ALL
        updateUI()

        all_button.setOnClickListener {
            clearSelection()
            all_button.isSelected = true
            chartIntervalType = ChartIntervalType.ALL
            updateUI()

        }

        hour_button.setOnClickListener {
            clearSelection()
            hour_button.isSelected = true
            chartIntervalType = ChartIntervalType.HOUR
            updateUI()
        }

        minute_button.setOnClickListener {
            clearSelection()
            minute_button.isSelected = true
            chartIntervalType = ChartIntervalType.MINUTE
            updateUI()
        }

    }

    private fun clearSelection() {
        all_button.isSelected = false
        hour_button.isSelected = false
        minute_button.isSelected = false
    }

    private fun updateUI() {
        if (this.chartIntervalType == ChartIntervalType.MINUTE) {
            showLinearCharts()
        } else if (this.chartIntervalType == ChartIntervalType.ALL) {
            showIntervalCharts(60, Int.MAX_VALUE)
        } else if (this.chartIntervalType == ChartIntervalType.HOUR) {
            showIntervalCharts(15, 60)
        }

    }

    private fun showIntervalCharts(minutes: Int, timeSpanInMinute: Int) {

        removeAllFragments()
        addFragment(
            LiveIntervalGraphFragment.newInstance(ReadingType.SP02, args.sessionStart, minutes, timeSpanInMinute),
            SPO2_INTERVAL_FRAGMENT_TAG
        )
        addFragment(
            LiveIntervalGraphFragment.newInstance(ReadingType.PR, args.sessionStart, minutes, timeSpanInMinute),
            PR_INTERVAL_FRAGMENT_TAG
        )
        addFragment(
            LiveIntervalGraphFragment.newInstance(ReadingType.RRP, args.sessionStart, minutes, timeSpanInMinute),
            RRP_INTERVAL_FRAGMENT_TAG
        )
    }

    private fun showLinearCharts() {
        removeAllFragments()

        addFragment(
            LiveLineGraphFragment.newInstance(ReadingType.SP02, args.sessionStart),
            SPO2_LINE_FRAGMENT_TAG
        )
        addFragment(
            LiveLineGraphFragment.newInstance(ReadingType.PR, args.sessionStart),
            PR_LINE_FRAGMENT_TAG
        )
        addFragment(
            LiveLineGraphFragment.newInstance(ReadingType.RRP, args.sessionStart),
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
}