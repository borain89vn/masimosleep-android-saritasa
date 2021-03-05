package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph.util.LineGraphViewData
import com.mymasimo.masimosleep.util.calculateXZoomScale
import com.mymasimo.masimosleep.util.getMaxChartValue
import com.mymasimo.masimosleep.util.getMinChartValue
import kotlinx.android.synthetic.main.fragment_report_line_graph.*
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

//TODO There should be a collection of those constants used by LineGraph
const val CHART_OFFSET_PERCENT = 0.1f
private const val BLOCK_SEPARATION_THRESHOLD_MILLIS = 3 * 60 * 1000 // 3 minutes.

class ReportLineGraphFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportLineGraphViewModel by viewModels { vmFactory }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_report_line_graph, container, false)

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

        chart_title.text = resources.getString(titleID)
        type_icon.setImageDrawable(resources.getDrawable(iconID, null))

        configureChart()
    }

    fun configureChart() {
        chart_live.description.isEnabled = false
        chart_live.setNoDataTextColor(resources.getColor(R.color.white, null))
        chart_live.isScaleYEnabled = false
        chart_live.isHighlightPerTapEnabled = false
        chart_live.isHighlightPerDragEnabled = false
        chart_live.legend.isEnabled = false

        val xAxis = chart_live.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.gridColor = resources.getColor(gridColorID, null)
        xAxis.axisLineColor = resources.getColor(gridColorID, null)
        xAxis.textColor = resources.getColor(xAxisColorID, null)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)

        val labelCount = 4
        xAxis.granularity = BLOCK_SEPARATION_THRESHOLD_MILLIS / labelCount.toFloat()
        xAxis.labelCount = labelCount

        val dateFormatter = SimpleDateFormat("hh:mm")
        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return dateFormatter.format(Date(value.toLong()))
            }
        }


        xAxis.valueFormatter = formatter

        val rightAxis = chart_live.axisRight
        rightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //font
        rightAxis.setDrawGridLines(true)
        rightAxis.setDrawAxisLine(true)
        rightAxis.isGranularityEnabled = true
        //dashed lines
        rightAxis.spaceTop = 0.1F
        rightAxis.spaceBottom = 0.1F
        rightAxis.yOffset = -9F
        rightAxis.gridColor = resources.getColor(gridColorID, null)
        rightAxis.textColor = resources.getColor(yAxisColorID, null)

        val leftAxis = chart_live.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID, null)

    }

    fun updateUI(lineGraphData: LineGraphViewData) {

        val avgRounded = lineGraphData.average
            .toBigDecimal()
            .setScale(1, RoundingMode.UP)
            .toDouble()



        avg_text.text = avgRounded.toString()
        updateChart(lineGraphData.points)
    }

    fun updateChart(pointLists: List<List<LineGraphViewData.LineGraphPoint>>) {

        val chartDataSets: ArrayList<ILineDataSet> = ArrayList()

        var minVal: Double = Double.MAX_VALUE
        var maxVal: Double = Double.MIN_VALUE

        var startTime: Long = Long.MAX_VALUE
        var endTime: Long = Long.MIN_VALUE

        for (pointList in pointLists) {

            val chartEntryList: ArrayList<Entry> = ArrayList()


            for (point in pointList) {

                chartEntryList.add(
                    Entry(point.timestamp.toFloat(), point.value.toFloat())
                )


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

            val lineDataSet = LineDataSet(chartEntryList, "")
            lineDataSet.lineWidth = 2.0F
            lineDataSet.color = resources.getColor(lineColorID, null)
            lineDataSet.setDrawCircles(false)
            lineDataSet.setDrawValues(false)
            chartDataSets.add(lineDataSet)

            val boundaryEntryList: ArrayList<Entry> = ArrayList()
            boundaryEntryList.add(
                Entry(
                    (startTime - BLOCK_SEPARATION_THRESHOLD_MILLIS * CHART_OFFSET_PERCENT),
                    getMinChartValue(readingType).toFloat()
                )
            )
            boundaryEntryList.add(
                Entry(
                    (endTime + BLOCK_SEPARATION_THRESHOLD_MILLIS * CHART_OFFSET_PERCENT),
                    getMaxChartValue(readingType).toFloat()
                )
            )
            val boundarySet = LineDataSet(boundaryEntryList, "")
            boundarySet.color = resources.getColor(R.color.clear, null)
            boundarySet.setDrawValues(false)
            boundarySet.setDrawCircles(false)
            chartDataSets.add(boundarySet)

            val lowRounded = minVal
                .toBigDecimal()
                .setScale(1, RoundingMode.UP)
                .toDouble()

            val highRounded = maxVal
                .toBigDecimal()
                .setScale(1, RoundingMode.UP)
                .toDouble()

            low_high_text.text = "$lowRounded - $highRounded"

        }

        val lineData: LineData = LineData(chartDataSets)


        chart_live.data = lineData

        chart_live.zoom(
            calculateXZoomScale(
                (BLOCK_SEPARATION_THRESHOLD_MILLIS).toLong(), startTime, endTime
            ),
            1F,
            endTime - (BLOCK_SEPARATION_THRESHOLD_MILLIS * (1 + CHART_OFFSET_PERCENT)) / 2,
            (getMaxChartValue(readingType).toFloat() + getMinChartValue(readingType).toFloat()) / 2
        )

        chart_live.invalidate()

    }

    companion object {
        private const val READING_TYPE_KEY = "READING_TYPE"
        private const val SESSION_ID_KEY = "SESSION_ID"

        private val gridColorID: Int = R.color.chart_grid_light
        private val xAxisColorID: Int = R.color.chart_x_label_light
        private val yAxisColorID: Int = R.color.chart_y_label_light
        private val lineColorID: Int = R.color.chart_line_light

        fun newInstance(type: ReadingType, sessionId: Long) = ReportLineGraphFragment().apply {
            arguments = bundleOf(
                READING_TYPE_KEY to type,
                SESSION_ID_KEY to sessionId
            )
        }
    }
}