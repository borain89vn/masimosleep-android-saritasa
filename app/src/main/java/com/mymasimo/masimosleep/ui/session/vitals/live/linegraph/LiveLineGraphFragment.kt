package com.mymasimo.masimosleep.ui.session.vitals.live.linegraph

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.data.AxisYData
import com.masimo.timelinechart.data.InputData
import com.masimo.timelinechart.formatter.AxisFormatter
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.databinding.FragmentLiveLineGraphBinding
import com.mymasimo.masimosleep.ui.session.vitals.live.linegraph.util.LineGraphViewData
import io.reactivex.disposables.CompositeDisposable
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class LiveLineGraphFragment : Fragment(R.layout.fragment_live_line_graph) {

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
        private const val SCALE_KEY = "SCALE"

        fun newInstance(type: ReadingType, startAt: Long, scale: Int) = LiveLineGraphFragment().apply {
            arguments = bundleOf(
                READING_TYPE_KEY to type,
                START_TIME_KEY to startAt,
                SCALE_KEY to scale,
            )
        }
    }

    private lateinit var readingType: ReadingType
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            readingType = getSerializable(READING_TYPE_KEY) as ReadingType
            startTime = getLong(START_TIME_KEY)
        }

        vm.onCreate(readingType, startTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        viewBinding.chartLive.setAxisXPageStep(10, 5)
        viewBinding.chartLive.setAxisXFormatter(object : AxisFormatter {
            override fun formatData(value: Float): String = SimpleDateFormat("hh:mm").format(Date(value.toLong()))
        })

        viewBinding.chartLive.setShowCirclePoint(true)
        viewBinding.chartLive.setLimitRange(97f, 90f)
        var titleID: Int = R.string.vital_title_SPO2
        var iconID: Int = R.drawable.spo2_icon

        if (readingType == ReadingType.PR) {
            titleID = R.string.vital_title_PR
            iconID = R.drawable.pr_icon
            viewBinding.chartLive.setLimitRange(100f, 40f)
        } else if (readingType == ReadingType.RRP) {
            titleID = R.string.vital_title_RRP
            iconID = R.drawable.rrp_icon
            viewBinding.chartLive.setLimitRange(18f, 2f)
        }

        viewBinding.chartTitle.text = resources.getString(titleID)
        viewBinding.typeIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, iconID, null))
    }

    private fun updateUI(lineGraphData: LineGraphViewData) {
        val avgRounded = lineGraphData.average
            .toBigDecimal()
            .setScale(1, RoundingMode.UP)
            .toDouble()

        viewBinding.avgText.text = avgRounded.toString()
        updateChart(lineGraphData.points)
    }

    private fun updateChart(pointLists: List<List<LineGraphViewData.LineGraphPoint>>) {
        val chartData: ArrayList<InputData> = ArrayList()

        var minVal: Double = Double.MAX_VALUE
        var maxVal: Double = Double.MIN_VALUE

        for (pointList in pointLists) {
            for (point in pointList) {
                chartData.add(InputData(point.timestamp.toFloat(), point.value.toFloat()))

                if (point.value < minVal) {
                    minVal = point.value
                }

                if (point.value > maxVal) {
                    maxVal = point.value
                }
            }

            val lowRounded = minVal
                .toBigDecimal()
                .setScale(1, RoundingMode.UP)
                .toDouble()

            val highRounded = maxVal
                .toBigDecimal()
                .setScale(1, RoundingMode.UP)
                .toDouble()

            viewBinding.lowHighText.text = "$lowRounded - $highRounded"
        }

        val axisYList = ArrayList<AxisYData>()
        when (readingType) {
            ReadingType.SP02 -> {
                axisYList.add(AxisYData(y = 100f))
                axisYList.add(AxisYData(y = 90f))
                axisYList.add(AxisYData(y = 80f))
                axisYList.add(AxisYData(y = 70f))
                axisYList.add(AxisYData(y = 60f))
                axisYList.add(AxisYData(y = 50f))
            }
            ReadingType.PR -> {
                axisYList.add(AxisYData(y = 160f))
                axisYList.add(AxisYData(y = 140f))
                axisYList.add(AxisYData(y = 120f))
                axisYList.add(AxisYData(y = 100f))
                axisYList.add(AxisYData(y = 80f))
                axisYList.add(AxisYData(y = 60f))
                axisYList.add(AxisYData(y = 40f))
                axisYList.add(AxisYData(y = 20f))
            }
            ReadingType.RRP -> {
                axisYList.add(AxisYData(y = 40f))
                axisYList.add(AxisYData(y = 31f))
                axisYList.add(AxisYData(y = 22f))
                axisYList.add(AxisYData(y = 13f))
                axisYList.add(AxisYData(y = 4f))
            }
        }

        viewBinding.chartLive.setData(chartData, axisYList)
        viewBinding.chartLive.invalidate()
    }
}