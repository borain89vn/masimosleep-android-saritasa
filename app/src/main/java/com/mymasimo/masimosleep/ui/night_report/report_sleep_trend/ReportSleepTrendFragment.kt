package com.mymasimo.masimosleep.ui.night_report.report_sleep_trend

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.data.AxisYData
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
        viewBinding.chartSleepScore.setShowCirclePoint(true)
    }

    private fun updateChart(trendData: ReportSleepTrendViewModel.SleepQualityTrendViewData) {
        val chartData: ArrayList<InputData> = ArrayList()

        for (point in trendData.intervals) {
            if (point.score.isNaN()) {
                continue
            }

            val time = point.startAt.toFloat()
            val score = (point.score * 100.0).toFloat()
            chartData.add(InputData(time, score))
        }

        val axisYList = ArrayList<AxisYData>()
        axisYList.add(AxisYData(y = 100f))
        axisYList.add(AxisYData(y = 80f))
        axisYList.add(AxisYData(y = 60f))
        axisYList.add(AxisYData(y = 40f))
        axisYList.add(AxisYData(y = 20f))
        axisYList.add(AxisYData(y = 0f))

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