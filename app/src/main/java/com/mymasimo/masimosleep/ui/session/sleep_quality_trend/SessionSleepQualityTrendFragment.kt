package com.mymasimo.masimosleep.ui.session.sleep_quality_trend

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.TimelineChartView
import com.masimo.timelinechart.data.*
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSessionSleepQualityTrendBinding
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.LocalDateTime
import org.joda.time.Seconds
import javax.inject.Inject
import kotlin.collections.ArrayList

class SessionSleepQualityTrendFragment : Fragment(R.layout.fragment_session_sleep_quality_trend),
    TimelineChartView.DataSource {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: SleepQualityTrendViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentSessionSleepQualityTrendBinding::bind)

    private var coordinates: ArrayList<Coordinate> = ArrayList()

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

        viewBinding.chartSleepScore.dataSource = this
    }

    private fun updateChart(trendData: SleepQualityTrendViewModel.SleepQualityTrendViewData) {
        coordinates = ArrayList()

        for (point in trendData.intervals.sortedBy { it.startAt }) {
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

        viewBinding.chartSleepScore.setMinMaxVisibleTimeInterval(interval, interval)
        viewBinding.chartSleepScore.setVisibleTimeInterval(interval, false)
    }

    override fun timelineChartViewLowerBoundDate(view: TimelineChartView): LocalDateTime {
        return coordinates.firstOrNull()?.dateTime ?: LocalDateTime.now()
    }

    override fun timelineChartViewUpperBoundDate(view: TimelineChartView): LocalDateTime {
        return coordinates.lastOrNull()?.dateTime ?: LocalDateTime.now()
    }

    override fun timelineChartViewAxisValues(view: TimelineChartView): List<AxisValue> {
        return listOf(0, 50, 100).map { AxisValue(it.toString(), it.toFloat() / 100f) }
    }

    override fun timelineChartViewZones(view: TimelineChartView): List<Zone> {
        val colorValues = listOf(
            Pair(R.color.sq_redOn, 55),
            Pair(R.color.sq_yellowOn, 75),
            Pair(R.color.sq_greenOn, 100)
        )
        return colorValues.map {
            Zone(
                resources.getColor(it.first, null),
                it.second.toFloat() / 100f
            )
        }
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
        return Seconds.seconds(0)
    }
}