package com.mymasimo.masimosleep.ui.session.sleep_quality_trend

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSessionSleepQualityTrendBinding
import com.mymasimo.masimosleep.util.getMaxChartValue
import com.mymasimo.masimosleep.util.getMinChartValue
import io.reactivex.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class SessionSleepQualityTrendFragment : Fragment(R.layout.fragment_session_sleep_quality_trend) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: SleepQualityTrendViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentSessionSleepQualityTrendBinding::bind)

    companion object {
        private const val START_TIME_KEY = "START_TIME"

        private const val gridColorID: Int = R.color.chart_grid_dark
        private const val xAxisColorID: Int = R.color.chart_x_label_dark
        private const val yAxisColorID: Int = R.color.chart_y_label_dark
        private const val lineColorID: Int = R.color.chart_line_dark

        fun newInstance(startAt: Long) = SessionSleepQualityTrendFragment().apply {
            arguments = bundleOf(
                START_TIME_KEY to startAt
            )
        }
    }

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            startTime = getLong(START_TIME_KEY)
        }

        vm.onCreated(startTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    private fun loadViewContent() {

        configureChart()

        vm.viewData.observe(viewLifecycleOwner) { trendData ->
            updateChart(trendData)
        }
    }


    private fun configureChart() {
        viewBinding.chartSleepScore.description.isEnabled = false
        viewBinding.chartSleepScore.setNoDataTextColor(resources.getColor(R.color.white, null))
        viewBinding.chartSleepScore.isScaleYEnabled = false
        viewBinding.chartSleepScore.isHighlightPerTapEnabled = false
        viewBinding.chartSleepScore.isHighlightPerDragEnabled = false
        viewBinding.chartSleepScore.legend.isEnabled = false

        val xAxis = viewBinding.chartSleepScore.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.gridColor = resources.getColor(gridColorID, null)
        xAxis.axisLineColor = resources.getColor(gridColorID, null)
        xAxis.textColor = resources.getColor(xAxisColorID, null)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)

        xAxis.granularity = 60000.0f
        xAxis.labelCount = 4

        val dateFormatter = SimpleDateFormat("hh:mm")
        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = dateFormatter.format(Date(value.toLong()))
        }

        xAxis.valueFormatter = formatter

        val rightAxis = viewBinding.chartSleepScore.axisRight
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

        val leftAxis = viewBinding.chartSleepScore.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID, null)
    }

    fun updateChart(trendData: SleepQualityTrendViewModel.SleepQualityTrendViewData) {

        val chartDataSets: ArrayList<ILineDataSet> = ArrayList()
        val chartEntryList: ArrayList<Entry> = ArrayList()
        val colorList: ArrayList<Int> = ArrayList()

        var startTime: Long = Long.MAX_VALUE
        var endTime: Long = Long.MIN_VALUE

        for (point in trendData.intervals) {

            if (point.score.isNaN()) {
                continue
            }

            val score = (point.score * 100.0).toFloat()
            chartEntryList.add(
                Entry(point.startAt.toFloat(), score)
            )


            var circleColorId = R.color.subtleGray
            if (score.toInt() <= resources.getInteger(R.integer.red_upper)) {
                circleColorId = R.color.sq_redOn
            } else if (score.toInt() <= resources.getInteger(R.integer.yellow_upper)) {
                circleColorId = R.color.sq_yellowOn
            } else if (score.toInt() > resources.getInteger(R.integer.yellow_upper)) {
                circleColorId = R.color.sq_greenOn
            }

            colorList.add(resources.getColor(circleColorId, null))


            if (point.startAt < startTime) {
                startTime = point.startAt
            }

            if (point.startAt > endTime) {
                endTime = point.startAt
            }

        }

        val lineDataSet = LineDataSet(chartEntryList, "")
        lineDataSet.color = resources.getColor(lineColorID, null)
        lineDataSet.circleColors = colorList
        lineDataSet.setDrawCircles(true)
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.circleRadius = 4.0f
        lineDataSet.lineWidth = 2.0f
        chartDataSets.add(lineDataSet)


        val boundaryEntryList: ArrayList<Entry> = ArrayList()
        boundaryEntryList.add(
            Entry(
                (startTime - 60 * 60 * 1800).toFloat(),
                getMinChartValue().toFloat()
            )
        )
        boundaryEntryList.add(
            Entry(
                (endTime + 60 * 60 * 1800).toFloat(),
                getMaxChartValue().toFloat()
            )
        )
        val boundarySet = LineDataSet(boundaryEntryList, "")
        boundarySet.color = resources.getColor(R.color.clear, null)
        boundarySet.setDrawCircles(false)
        boundarySet.setDrawValues(false)
        chartDataSets.add(boundarySet)

        val lineData: LineData = LineData(chartDataSets)

        viewBinding.chartSleepScore.data = lineData
        viewBinding.chartSleepScore.invalidate()
    }
}