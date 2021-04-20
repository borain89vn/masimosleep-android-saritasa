package com.mymasimo.masimosleep.ui.session.sleep_quality_trend

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
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSessionSleepQualityTrendBinding
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
        vm.viewData.observe(viewLifecycleOwner) { trendData ->
            updateChart(trendData)
        }

        viewBinding.chartSleepScore.setAxisXPageStep(10, 5)
        viewBinding.chartSleepScore.setAxisXFormatter(object : AxisFormatter {
            override fun formatData(value: Float): String = SimpleDateFormat("hh:mm").format(Date(value.toLong()))
        })
    }

    private fun updateChart(trendData: SleepQualityTrendViewModel.SleepQualityTrendViewData) {
        val chartData: ArrayList<InputData> = ArrayList()
        val colorList: ArrayList<Int> = ArrayList()

        var startTime: Long = Long.MAX_VALUE
        var endTime: Long = Long.MIN_VALUE

        for (point in trendData.intervals) {
            if (point.score.isNaN()) {
                continue
            }

            val score = (point.score * 100.0).toFloat()
            chartData.add(InputData(point.startAt.toFloat(), score))

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
}