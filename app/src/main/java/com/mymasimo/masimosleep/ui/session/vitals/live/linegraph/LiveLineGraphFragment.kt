package com.mymasimo.masimosleep.ui.session.vitals.live.linegraph

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.EdgeInsets
import com.masimo.timelinechart.TimelineChartView
import com.masimo.timelinechart.ViewStyle
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.databinding.FragmentLiveLineGraphBinding
import com.mymasimo.masimosleep.model.LineGraphViewData
import com.mymasimo.masimosleep.model.VitalsChartDataSource
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.Seconds
import java.math.RoundingMode
import javax.inject.Inject

class LiveLineGraphFragment : Fragment(R.layout.fragment_live_line_graph), TimelineChartView.Delegate {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: LineGraphViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentLiveLineGraphBinding::bind)

    companion object {
        private const val READING_TYPE_KEY = "READING_TYPE"
        private const val START_TIME_KEY = "START_TIME"
        private const val VIEW_STYLE_KEY = "VIEW_STYLE"

        fun newInstance(type: ReadingType, startAt: Long, viewStyle: ViewStyle) = LiveLineGraphFragment().apply {
            arguments = bundleOf(
                READING_TYPE_KEY to type,
                START_TIME_KEY to startAt,
                VIEW_STYLE_KEY to viewStyle,
            )
        }
    }

    private lateinit var readingType: ReadingType
    private lateinit var chartViewStyle: ViewStyle
    private lateinit var dataSource: VitalsChartDataSource
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            readingType = getSerializable(READING_TYPE_KEY) as ReadingType
            chartViewStyle = getSerializable(VIEW_STYLE_KEY) as ViewStyle
            startTime = getLong(START_TIME_KEY)
        }

        vm.onCreate(readingType, startTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataSource = VitalsChartDataSource(true, view.context, readingType)
        dataSource.switchViewStyle(chartViewStyle)

        loadViewContent()

        vm.lineGraphViewData.observe(viewLifecycleOwner) { lineGraphData ->
            updateUI(lineGraphData)
        }
        vm.currentReading.observe(viewLifecycleOwner) { currentReading ->
            val currentRounded = currentReading
                .toBigDecimal()
                .setScale(1, RoundingMode.UP)
                .toDouble()
            viewBinding.currentText.text = currentRounded.toString()
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

        viewBinding.chartTitle.text = resources.getString(titleID)
        viewBinding.typeIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, iconID, null))

        viewBinding.chartLive.goButtonIconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_chart_forward_dark, null)
        viewBinding.chartLive.goButtonBackgrounColor = resources.getColor(R.color.trayText, null)
        viewBinding.chartLive.textColor = resources.getColor(R.color.trayText, null)
        viewBinding.chartLive.gridColor = resources.getColor(R.color.trayText, null)
        viewBinding.chartLive.plotInsets = EdgeInsets(10, 10, 40, 35)
        viewBinding.chartLive.setMinMaxVisibleTimeInterval(
            Seconds.seconds(5 * 60),
            Seconds.seconds(24 * 60 * 60)
        )
        viewBinding.chartLive.setVisibleTimeInterval(chartViewStyle.preferredVisibleTimeInterval(), true)
        viewBinding.chartLive.dataSource = dataSource
        viewBinding.chartLive.delegate = this
    }

    private fun updateUI(lineGraphData: LineGraphViewData) {
        val avgRounded = lineGraphData.average
            .toBigDecimal()
            .setScale(1, RoundingMode.UP)
            .toDouble()

        viewBinding.avgText.text = avgRounded.toString()
        updateChart(lineGraphData.points)
    }

    private fun updateChart(points: List<LineGraphViewData.LineGraphPoint>) {
        dataSource.update(points)
        viewBinding.lowHighText.text = dataSource.lowHighText
        viewBinding.chartLive.reloadData()
    }

    override fun timelineChartViewDidEndZoom(view: TimelineChartView) {
        super.timelineChartViewDidEndZoom(view)
        val newViewStyle = ViewStyle.styleFromVisibleTimeInterval(view.visibleTimeInterval)
        if (newViewStyle != chartViewStyle) {
            chartViewStyle = newViewStyle
            dataSource.switchViewStyle(newViewStyle)
            view.reloadData()
        }
    }
}