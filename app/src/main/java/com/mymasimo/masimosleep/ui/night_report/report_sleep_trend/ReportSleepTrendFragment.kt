package com.mymasimo.masimosleep.ui.night_report.report_sleep_trend

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
import com.mymasimo.masimosleep.util.getMaxChartValue
import com.mymasimo.masimosleep.util.getMinChartValue
import kotlinx.android.synthetic.main.fragment_report_sleep_trend.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ReportSleepTrendFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportSleepTrendViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreated(requireArguments().getLong(KEY_SESSION_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_sleep_trend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.viewData.observe(viewLifecycleOwner) { viewData ->
            updateChart(viewData)
        }

        configureChart()
    }

    private fun configureChart() {
        chart_sleep_score.description.isEnabled = false
        chart_sleep_score.setNoDataTextColor(resources.getColor(R.color.white,null))
        chart_sleep_score.isScaleYEnabled = false
        chart_sleep_score.isHighlightPerTapEnabled = false
        chart_sleep_score.isHighlightPerDragEnabled = false
        chart_sleep_score.legend.isEnabled = false

        val xAxis = chart_sleep_score.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.gridColor = resources.getColor(gridColorID,null)
        xAxis.axisLineColor = resources.getColor(gridColorID,null)
        xAxis.textColor = resources.getColor(xAxisColorID,null)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)

        xAxis.granularity = 60000.0f
        xAxis.labelCount = 4

        val dateFormatter = SimpleDateFormat("hh:mm")
        val formatter = object :  ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val date = dateFormatter.format(Date(value.toLong()))

                return date
            }
        }

        xAxis.valueFormatter = formatter


        val rightAxis = chart_sleep_score.axisRight
        rightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //font
        rightAxis.setDrawGridLines(true)
        rightAxis.setDrawAxisLine(true)
        rightAxis.isGranularityEnabled = true
        //dashed lines
        rightAxis.spaceTop = 0.1F
        rightAxis.spaceBottom = 0.1F
        rightAxis.yOffset = -9F
        rightAxis.gridColor = resources.getColor(gridColorID,null)
        rightAxis.textColor = resources.getColor(yAxisColorID,null)

        val leftAxis = chart_sleep_score.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID,null)
    }

    private fun updateChart(trendData : ReportSleepTrendViewModel.SleepQualityTrendViewData) {

        val chartDataSets : ArrayList<ILineDataSet> = ArrayList<ILineDataSet>()
        val chartEntryList : ArrayList<Entry> = ArrayList<Entry>()
        val colorList : ArrayList<Int> = ArrayList()

        var startTime : Long = Long.MAX_VALUE
        var endTime : Long = Long.MIN_VALUE

        for (point in trendData.intervals) {

            if (point.score.isNaN()) {
                continue
            }

            val score = (point.score * 100.0).toFloat()
            chartEntryList.add(
                Entry(point.startAt.toFloat(),score)
            )


            var circleColorId = R.color.subtleGray
            if (score.toInt() <= resources.getInteger(R.integer.red_upper)) {
                circleColorId = R.color.sq_redOn
            } else if (score.toInt() <= resources.getInteger(R.integer.yellow_upper)) {
                circleColorId = R.color.sq_yellowOn
            } else if (score.toInt() > resources.getInteger(R.integer.yellow_upper)) {
                circleColorId = R.color.sq_greenOn
            }

            colorList.add(resources.getColor(circleColorId,null))


            if (point.startAt < startTime) {
                startTime = point.startAt
            }

            if (point.startAt > endTime) {
                endTime = point.startAt
            }

        }

        val lineDataSet = LineDataSet(chartEntryList,"")
        lineDataSet.color = resources.getColor(lineColorID,null)
        lineDataSet.circleColors = colorList
        lineDataSet.setDrawCircles(true)
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.circleRadius = 4.0f
        lineDataSet.lineWidth = 2.0f
        chartDataSets.add(lineDataSet)


        val boundaryEntryList : ArrayList<Entry> = ArrayList<Entry>()
        boundaryEntryList.add(
            Entry((startTime - 60*60*1800).toFloat(),
                  getMinChartValue().toFloat())
        )
        boundaryEntryList.add(
            Entry((endTime + 60*60*1800).toFloat(),
                  getMaxChartValue().toFloat())
        )
        val boundarySet = LineDataSet(boundaryEntryList,"")
        boundarySet.color = resources.getColor(R.color.clear,null)
        boundarySet.setDrawCircles(false)
        boundarySet.setDrawValues(false)
        chartDataSets.add(boundarySet)

        val lineData : LineData = LineData(chartDataSets)


        chart_sleep_score.data = lineData
        chart_sleep_score.invalidate()
    }

    companion object {
        private val gridColorID : Int = R.color.chart_grid_light
        private val xAxisColorID : Int = R.color.chart_x_label_light
        private val yAxisColorID : Int = R.color.chart_y_label_light
        private val lineColorID : Int = R.color.chart_line_light

        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportSleepTrendFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}