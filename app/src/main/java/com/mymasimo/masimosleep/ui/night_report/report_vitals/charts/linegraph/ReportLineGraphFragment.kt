package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.data.InputData
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.databinding.FragmentReportLineGraphBinding
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph.util.LineGraphViewData
import java.math.RoundingMode
import javax.inject.Inject

class ReportLineGraphFragment : Fragment(R.layout.fragment_report_line_graph) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportLineGraphViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportLineGraphBinding::bind)

    private lateinit var readingType: ReadingType
    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            readingType = getSerializable(READING_TYPE_KEY) as ReadingType
            sessionId = getLong(SESSION_ID_KEY)
        }

        vm.onCreate(readingType, sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()

        vm.lineGraphViewData.observe(viewLifecycleOwner) { lineGraphData ->
            updateUI(lineGraphData)
        }
    }

    private fun loadViewContent() {
        var titleID: Int = R.string.vital_title_SPO2
        var iconID: Int = R.drawable.spo2_icon

        if (readingType == ReadingType.PR) {
            titleID = R.string.vital_title_PR
            iconID = R.drawable.pr_icon
        } else if (readingType == ReadingType.RRP) {
            titleID = R.string.vital_title_RRP
            iconID = R.drawable.rrp_icon
        }

        viewBinding.chartTitle.text = resources.getString(titleID)
        viewBinding.typeIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, iconID, null))
    }

    private fun updateUI(lineGraphData: LineGraphViewData) {
        val avgRounded = lineGraphData.average
            .toBigDecimal()
            .setScale(1, RoundingMode.UP)
            .toDouble()

        viewBinding.avgText.text = avgRounded.toString()
        updateChart(lineGraphData.points)
    }

    private fun updateChart(pointLists: List<List<LineGraphViewData.LineGraphPoint>>) {
        val chartData: ArrayList<InputData> = ArrayList()

        var minVal: Double = Double.MAX_VALUE
        var maxVal: Double = Double.MIN_VALUE

        var startTime: Long = Long.MAX_VALUE
        var endTime: Long = Long.MIN_VALUE

        for (pointList in pointLists) {
            for (point in pointList) {
                chartData.add(InputData(point.value.toInt()))

                if (point.value < minVal) {
                    minVal = point.value
                }

                if (point.value > maxVal) {
                    maxVal = point.value
                }

                if (point.timestamp < startTime) {
                    startTime = point.timestamp
                }

                if (point.timestamp > endTime) {
                    endTime = point.timestamp
                }

            }

            val lowRounded = minVal
                .toBigDecimal()
                .setScale(1, RoundingMode.UP)
                .toDouble()

            val highRounded = maxVal
                .toBigDecimal()
                .setScale(1, RoundingMode.UP)
                .toDouble()

            viewBinding.lowHighText.text = "$lowRounded - $highRounded"
        }

        viewBinding.chartLive.setData(chartData, ArrayList())
        viewBinding.chartLive.invalidate()
    }

    companion object {
        private const val READING_TYPE_KEY = "READING_TYPE"
        private const val SESSION_ID_KEY = "SESSION_ID"
        private const val SCALE_KEY = "SCALE"

        fun newInstance(type: ReadingType, sessionId: Long, scale: Int) = ReportLineGraphFragment().apply {
            arguments = bundleOf(
                READING_TYPE_KEY to type,
                SESSION_ID_KEY to sessionId,
                SCALE_KEY to scale,
            )
        }
    }
}