package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.Interval
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.IntervalGraphViewData
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.TimeSpan
import com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.intervalgraph.util.toMillis
import com.mymasimo.masimosleep.ui.session.vitals.live.intervalgraph.CHART_OFFSET_PERCENT
import com.mymasimo.masimosleep.util.calculateXZoomScale
import com.mymasimo.masimosleep.util.getChartColorForValue
import com.mymasimo.masimosleep.util.getMaxChartValue
import com.mymasimo.masimosleep.util.getMinChartValue
import kotlinx.android.synthetic.main.fragment_report_interval_graph.*
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReportIntervalGraphFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportIntervalGraphViewModel by viewModels { vmFactory }

    private lateinit var readingType: ReadingType
    private var sessionId: Long = -1
    private var minutes: Int = 1
    private var timeSpanInMinutes: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            readingType = getSerializable(READING_TYPE_KEY) as ReadingType
            sessionId = getLong(SESSION_ID_KEY)
            minutes = getInt(MINUTES_KEY)
            timeSpanInMinutes = getInt(TIME_SPAN_IN_MINUTES_KEY)
        }

        vm.onCreate(readingType, sessionId, Interval(minutes), TimeSpan(timeSpanInMinutes))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_report_interval_graph, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()

        vm.intervalGraphViewData.observe(viewLifecycleOwner) { intervalData ->
            updateUI(intervalData)
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

    private fun configureChart() {
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
        xAxis.isGranularityEnabled = true
        xAxis.granularity = TimeSpan(timeSpanInMinutes).toMillis() / (labelCount.toFloat())
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

    private fun updateUI(intervalGraphData: IntervalGraphViewData) {

        val avgRounded = intervalGraphData.average
            .toBigDecimal()
            .setScale(1, RoundingMode.UP)
            .toDouble()

        avg_text.text = avgRounded.toString()

        updateChart(intervalGraphData)
    }

    private fun updateChart(intervalData: IntervalGraphViewData) {
        val chartDataSets: ArrayList<IScatterDataSet> = ArrayList()

        var minVal: Double = Double.MAX_VALUE
        var maxVal: Double = Double.MIN_VALUE

        var startTime: Long = Long.MAX_VALUE
        var endTime: Long = Long.MIN_VALUE

        val colorID = getChartColorForValue(requireContext(), readingType, intervalData.average)

        for (pointSet in intervalData.intervals) {
            val chartEntryList: ArrayList<Entry> = ArrayList()
            val colorList: ArrayList<Int> = ArrayList()
            for (value in pointSet.values) {

                chartEntryList.add(
                    Entry(pointSet.startAt.toFloat(), value.toFloat())
                )

                colorList.add(resources.getColor(colorID, null))

                if (value < minVal) {
                    minVal = value
                }

                if (value > maxVal) {
                    maxVal = value
                }
            }

            if (pointSet.startAt < startTime) {
                startTime = pointSet.startAt
            }

            if (pointSet.startAt > endTime) {
                endTime = pointSet.startAt
            }

            val scatterDataSet = ScatterDataSet(chartEntryList, "")
            scatterDataSet.colors = colorList
            scatterDataSet.scatterShapeSize = 10.0f
            scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            chartDataSets.add(scatterDataSet)

        }
        val scatterData: ScatterData = ScatterData(chartDataSets)

        val boundaryEntryList: ArrayList<Entry> = ArrayList()
        boundaryEntryList.add(
            Entry(
                (startTime - intervalData.timeSpan.toMillis() * CHART_OFFSET_PERCENT),
                getMinChartValue(readingType).toFloat()
            )
        )
        boundaryEntryList.add(
            Entry(
                endTime + intervalData.timeSpan.toMillis() * CHART_OFFSET_PERCENT,
                getMaxChartValue(readingType).toFloat()
            )
        )

        val boundaryDataSet = ScatterDataSet(boundaryEntryList, "")
        boundaryDataSet.color = resources.getColor(R.color.clear, null)
        boundaryDataSet.setDrawValues(false)
        scatterData.addDataSet(boundaryDataSet)

        scatterData.setDrawValues(false)


        chart_live.data = scatterData

        chart_live.zoom(
            calculateXZoomScale(
                TimeSpan(timeSpanInMinutes).toMillis(), startTime, endTime
            ),
            1F,
            endTime - (TimeSpan(timeSpanInMinutes).toMillis() * (1 + CHART_OFFSET_PERCENT)) / 2,
            (getMaxChartValue(readingType).toFloat() + getMinChartValue(readingType).toFloat()) / 2
        )

        chart_live.invalidate()

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

    companion object {
        private const val READING_TYPE_KEY = "READING_TYPE"
        private const val SESSION_ID_KEY = "SESSION_ID"
        private const val MINUTES_KEY = "MINUTES"
        private const val TIME_SPAN_IN_MINUTES_KEY = "TIME_SPAN_IN_MINUTES"

        private val gridColorID: Int = R.color.chart_grid_light
        private val xAxisColorID: Int = R.color.chart_x_label_light
        private val yAxisColorID: Int = R.color.chart_y_label_light
        private val lineColorID: Int = R.color.chart_line_light

        fun newInstance(type: ReadingType, sessionId: Long, minutes: Int, timeSpanInMinutes: Int) = ReportIntervalGraphFragment().apply {
            arguments = bundleOf(
                READING_TYPE_KEY to type,
                SESSION_ID_KEY to sessionId,
                MINUTES_KEY to minutes,
                TIME_SPAN_IN_MINUTES_KEY to timeSpanInMinutes
            )
        }
    }
}