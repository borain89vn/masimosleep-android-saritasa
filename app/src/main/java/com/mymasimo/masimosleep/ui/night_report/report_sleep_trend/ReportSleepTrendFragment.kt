package com.mymasimo.masimosleep.ui.night_report.report_sleep_trend

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.DataSource
import com.masimo.timelinechart.EdgeInsets
import com.masimo.timelinechart.TimelineChartView
import com.masimo.timelinechart.data.*
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportSleepTrendBinding
import org.joda.time.LocalDateTime
import org.joda.time.Seconds
import javax.inject.Inject
import kotlin.collections.ArrayList

class ReportSleepTrendFragment : Fragment(R.layout.fragment_report_sleep_trend), DataSource {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportSleepTrendViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportSleepTrendBinding::bind)
    private var coordinates: ArrayList<Coordinate> = ArrayList()

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

        viewBinding.chartSleepScore.plotInsets = EdgeInsets(5, 5, 35, 30)
        viewBinding.chartSleepScore.dataSource = this
    }

    private fun updateChart(trendData: ReportSleepTrendViewModel.SleepQualityTrendViewData) {
        coordinates = ArrayList()
        for (point in trendData.intervals) {
            if (point.score.isNaN()) {
                continue
            }
            coordinates.add(Coordinate(LocalDateTime(point.startAt), point.score.toFloat()))
        }

        val start = coordinates.firstOrNull()?.dateTime
        val end = coordinates.lastOrNull()?.dateTime
        val interval = if (start != null && end != null) {
            Seconds.secondsBetween(start, end)
        } else {
            Seconds.seconds(8 * 60 * 60)
        }
        viewBinding.chartSleepScore.setMinMaxVisibleTimeInterval(Seconds.seconds(60 * 60), interval)
        viewBinding.chartSleepScore.setVisibleTimeInterval(interval, false)
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportSleepTrendFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }

    override fun timelineChartViewLowerBoundDate(view: TimelineChartView): LocalDateTime {
        return coordinates.firstOrNull()?.dateTime ?: LocalDateTime.now()
    }

    override fun timelineChartViewUpperBoundDate(view: TimelineChartView): LocalDateTime {
        return coordinates.lastOrNull()?.dateTime ?: LocalDateTime.now()
    }

    override fun timelineChartViewAxisValues(view: TimelineChartView): List<AxisValue> {
        val list = listOf(0, 20, 40, 60, 80, 100)
        return list.map { AxisValue(it.toString(), it.toFloat() / 100.0f) }
    }

    override fun timelineChartViewZones(view: TimelineChartView): List<Zone> {
        // TODO: Need actual values for zones.
        return listOf(Zone(resources.getColor(R.color.optimal, null), 1.0f))
    }

    override fun timelineChartViewCoordinateSections(
        view: TimelineChartView,
        dateRange: Pair<LocalDateTime, LocalDateTime>
    ): List<List<Coordinate>> {
        return listOf(coordinates)
    }

    override fun timelineChartViewMarkers(
        view: TimelineChartView,
        dateRange: Pair<LocalDateTime, LocalDateTime>
    ): List<Marker> {
        return emptyList()
    }

    override fun timelineChartViewPrefetchInterval(view: TimelineChartView): Seconds {
        return Seconds.seconds(60 * 60)
    }
}