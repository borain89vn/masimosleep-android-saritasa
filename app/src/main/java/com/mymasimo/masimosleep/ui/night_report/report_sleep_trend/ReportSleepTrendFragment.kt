package com.mymasimo.masimosleep.ui.night_report.report_sleep_trend

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.data.InputData
import com.masimo.timelinechart.formatter.AxisFormatter
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportSleepTrendBinding
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ReportSleepTrendFragment : Fragment(R.layout.fragment_report_sleep_trend) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportSleepTrendViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportSleepTrendBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreated(requireArguments().getLong(KEY_SESSION_ID))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.viewData.observe(viewLifecycleOwner) { viewData ->
            updateChart(viewData)
        }

        viewBinding.chartSleepScore.setAxisXPageStep(10, 5)
        viewBinding.chartSleepScore.setAxisXFormatter(object : AxisFormatter {
            override fun formatData(value: Float): String = SimpleDateFormat("hh:mm").format(Date(value.toLong()))
        })
    }

    private fun updateChart(trendData: ReportSleepTrendViewModel.SleepQualityTrendViewData) {
        val chartData: ArrayList<InputData> = ArrayList()
        val colorList: ArrayList<Int> = ArrayList()

        var startTime: Long = Long.MAX_VALUE
        var endTime: Long = Long.MIN_VALUE

        for (point in trendData.intervals) {
            if (point.score.isNaN()) {
                continue
            }

            val time = point.startAt.toFloat()
            val score = (point.score * 100.0).toFloat()
            chartData.add(InputData(time, score))

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

        viewBinding.chartSleepScore.setData(chartData, ArrayList())
        viewBinding.chartSleepScore.invalidate()
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportSleepTrendFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}