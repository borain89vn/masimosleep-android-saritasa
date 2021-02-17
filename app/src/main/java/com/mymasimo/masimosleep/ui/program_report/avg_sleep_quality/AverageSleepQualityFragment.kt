package com.mymasimo.masimosleep.ui.program_report.avg_sleep_quality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.program_report.outcome.SleepOutcome
import com.mymasimo.masimosleep.util.getMaxChartValue
import com.mymasimo.masimosleep.util.getMinChartValue
import kotlinx.android.synthetic.main.fragment_average_sleep_quality.*
import java.util.*
import javax.inject.Inject

class AverageSleepQualityFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: AverageSleepQualityViewModel by viewModels { vmFactory }

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_average_sleep_quality, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()

        vm.score.observe(viewLifecycleOwner) { score ->
            updateScore(score.first, score.second)
        }

        vm.trendData.observe(viewLifecycleOwner) { viewDate ->
            updateChart(viewDate)
        }

        vm.sleepQualityDesc.observe(viewLifecycleOwner) { pairOfScoreAndOutcome ->
            updateSleepQualityDesc(pairOfScoreAndOutcome)
        }
    }

    private fun loadViewContent() {
        info_button.setOnClickListener {
            requireView().findNavController().navigate(
                    R.id.action_programReportFragment_to_sleepQualityDescriptionFragment
            )
        }

        configureChart()
    }

    private fun updateSleepQualityDesc(pairOfScoreAndOutcome: Triple<Double, Double, Int>) {
        if (pairOfScoreAndOutcome.third >= NUM_OF_NIGHTS - 1) {
            val scoreInt = (pairOfScoreAndOutcome.first * 100).toInt()
            val outcome = SleepOutcome.fromValue(pairOfScoreAndOutcome.second)

            var qualityDesc = R.string.program_quality_desc_poor
            when {
                scoreInt <= resources.getInteger(R.integer.red_upper)                                                            -> {
                    //POOR
                    qualityDesc = R.string.program_quality_desc_poor
                }
                scoreInt <= resources.getInteger(R.integer.yellow_upper) && scoreInt > resources.getInteger(R.integer.red_upper) -> {
                    //FAIR
                    qualityDesc = when (outcome) {
                        SleepOutcome.SLIGHT, SleepOutcome.SIGNIFICANT   -> {
                            R.string.program_quality_desc_fair_trend_up
                        }

                        SleepOutcome.TRENDING_DOWN, SleepOutcome.STABLE -> {
                            R.string.program_quality_desc_fair_trend_down
                        }

                    }
                }
                scoreInt > resources.getInteger(R.integer.yellow_upper)                                                          -> {
                    qualityDesc = R.string.program_quality_desc_good
                }
            }
            quality_desc.text = resources.getString(qualityDesc)
        }
    }

    private fun updateScore(score: Double, sessionCount: Int) {
        if (sessionCount >= NUM_OF_NIGHTS - 1) {
            quality_so_far_text.text = getString(R.string.average_sleep_quality_index)

            val scoreInt = (score * 100).toInt()
            lbl_score_text.text = scoreInt.toString()

            var face = R.drawable.face_red
            var qualityLevel = R.string.sq_redLabel

            when {
                scoreInt <= resources.getInteger(R.integer.red_upper)                                                            -> {

                }
                scoreInt <= resources.getInteger(R.integer.yellow_upper) && scoreInt > resources.getInteger(R.integer.red_upper) -> {

                    face = R.drawable.face_yellow
                    qualityLevel = R.string.sq_yellowLabel
                }
                scoreInt > resources.getInteger(R.integer.yellow_upper)                                                          -> {

                    face = R.drawable.face_green
                    qualityLevel = R.string.sq_greenLabel
                }
            }

            face_image.setImageDrawable(resources.getDrawable(face, null))
            quality_text.text = resources.getString(qualityLevel)
        } else {
            quality_so_far_text.text = getString(R.string.program_report_not_enough_nights)
        }
    }

    private fun configureChart() {
        chart_sleep_score.description.isEnabled = false
        chart_sleep_score.setNoDataTextColor(resources.getColor(R.color.white, null))
        chart_sleep_score.isScaleYEnabled = false
        chart_sleep_score.isHighlightPerTapEnabled = false
        chart_sleep_score.isHighlightPerDragEnabled = false
        chart_sleep_score.legend.isEnabled = false

        val xAxis = chart_sleep_score.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.gridColor = resources.getColor(gridColorID, null)
        xAxis.axisLineColor = resources.getColor(gridColorID, null)
        xAxis.textColor = resources.getColor(xAxisColorID, null)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)
        xAxis.setCenterAxisLabels(false)
        xAxis.granularity = 1.0f
        xAxis.labelCount = NUM_OF_NIGHTS

        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {

                return value.toInt().toString()
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
        rightAxis.gridColor = resources.getColor(gridColorID, null)
        rightAxis.textColor = resources.getColor(yAxisColorID, null)

        val leftAxis = chart_sleep_score.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID, null)
    }

    private fun updateChart(
            trendData: AverageSleepQualityViewModel.ProgramSleepQualityTrendViewData
    ) {

        val chartDataSets: ArrayList<ILineDataSet> = ArrayList<ILineDataSet>()
        val chartEntryList: ArrayList<Entry> = ArrayList<Entry>()
        val colorList: ArrayList<Int> = ArrayList()


        for (point in trendData.sessions) {

            if (point.score.isNaN()) {
                continue
            }

            val score = (point.score * 100.0).toFloat()
            val night = point.index + 1
            chartEntryList.add(
                    Entry(night.toFloat(), score)
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

        val boundaryEntryList: ArrayList<Entry> = ArrayList<Entry>()
        boundaryEntryList.add(
                Entry(0.0f, getMinChartValue().toFloat())
        )
        boundaryEntryList.add(
                Entry((NUM_OF_NIGHTS + 1).toFloat(), getMaxChartValue().toFloat())
        )
        val boundarySet = LineDataSet(boundaryEntryList, "")
        boundarySet.color = resources.getColor(R.color.clear, null)
        boundarySet.setDrawCircles(false)
        boundarySet.setDrawValues(false)
        chartDataSets.add(boundarySet)

        val lineData: LineData = LineData(chartDataSets)


        chart_sleep_score.data = lineData
        chart_sleep_score.invalidate()
    }

    companion object {

        private val gridColorID: Int = R.color.chart_grid_light
        private val xAxisColorID: Int = R.color.chart_x_label_light
        private val yAxisColorID: Int = R.color.chart_y_label_light
        private val lineColorID: Int = R.color.chart_line_light

        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = AverageSleepQualityFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }
}