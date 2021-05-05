package com.mymasimo.masimosleep.ui.night_report.report_vitals.charts.linegraph

import android.graphics.Color
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
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.databinding.FragmentReportLineGraphBinding
import com.mymasimo.masimosleep.model.LineGraphViewData
import com.mymasimo.masimosleep.model.VitalsChartDataSource
import org.joda.time.Seconds
import java.math.RoundingMode
import javax.inject.Inject

class ReportLineGraphFragment : Fragment(R.layout.fragment_report_line_graph), TimelineChartView.Delegate {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportLineGraphViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportLineGraphBinding::bind)

    private lateinit var readingType: ReadingType
    private lateinit var chartViewStyle: ViewStyle
    private lateinit var dataSource: VitalsChartDataSource
    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            readingType = getSerializable(READING_TYPE_KEY) as ReadingType
            chartViewStyle = getSerializable(VIEW_STYLE_KEY) as ViewStyle
            sessionId = getLong(SESSION_ID_KEY)
        }

        vm.onCreate(readingType, sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataSource = VitalsChartDataSource(false, view.context, readingType)
        dataSource.switchViewStyle(chartViewStyle)

        loadViewContent()

        vm.lineGraphViewData.observe(viewLifecycleOwner) { lineGraphData ->
            updateUI(lineGraphData)
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
        viewBinding.chartLive.goButtonBackgrounColor = Color.WHITE
        viewBinding.chartLive.plotInsets = EdgeInsets(10, 10, 40, 35)
        viewBinding.chartLive.setMinMaxVisibleTimeInterval(
            Seconds.seconds(5 * 60),
            Seconds.seconds(24 * 60 * 60)
        )
        viewBinding.chartLive.setVisibleTimeInterval(
            chartViewStyle.preferredVisibleTimeInterval(),
            false
        )
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

    companion object {
        private const val READING_TYPE_KEY = "READING_TYPE"
        private const val SESSION_ID_KEY = "SESSION_ID"
        private const val VIEW_STYLE_KEY = "VIEW_STYLE"

        fun newInstance(type: ReadingType, sessionId: Long, viewStyle: ViewStyle) =
            ReportLineGraphFragment().apply {
                arguments = bundleOf(
                    READING_TYPE_KEY to type,
                    SESSION_ID_KEY to sessionId,
                    VIEW_STYLE_KEY to viewStyle,
                )
            }
    }
}