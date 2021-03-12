package com.mymasimo.masimosleep.ui.session.session_events

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentSessionEventsBinding
import com.mymasimo.masimosleep.ui.session.session_events.util.SleepEventsViewData
import io.reactivex.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SessionEventsFragment : Fragment(R.layout.fragment_session_events) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: SessionSleepEventsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentSessionEventsBinding::bind)

    companion object {
        private const val START_TIME_KEY = "START_TIME"

        private const val gridColorID: Int = R.color.chart_grid_dark
        private const val xAxisColorID: Int = R.color.chart_x_label_dark
        private const val yAxisColorID: Int = R.color.chart_y_label_dark

        fun newInstance(startAt: Long) = SessionEventsFragment().apply {
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

        vm.onCreate(startTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.noEventsText.text = getString(R.string.sleep_events_empty, MasimoSleepPreferences.name)
        noEventsConfiguration()

        vm.sleepEvents.observe(viewLifecycleOwner) { sleepEventData ->
            updateUI(sleepEventData)
        }

    }

    private fun noEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.VISIBLE
        viewBinding.eventTray.visibility = View.GONE
        viewBinding.chartEvents.visibility = View.GONE
    }

    private fun receivedEventsConfiguration() {
        configureChart()
        viewBinding.noEventsTray.visibility = View.GONE
        viewBinding.eventTray.visibility = View.VISIBLE
        viewBinding.chartEvents.visibility = View.VISIBLE
    }

    private fun updateUI(sleepEventData: SleepEventsViewData) {
        val totalEvents = sleepEventData.totalEvents

        if (totalEvents == 0) {
            noEventsConfiguration()
        } else {
            receivedEventsConfiguration()
        }

        viewBinding.eventText.text = resources.getQuantityString(R.plurals.events_occurred, totalEvents, totalEvents)

        viewBinding.minorEventText.text = sleepEventData.minorEvents.toString()
        viewBinding.majorEventText.text = sleepEventData.majorEvents.toString()

        updateChart(sleepEventData.eventsByHour)
    }

    fun configureChart() {
        viewBinding.chartEvents.description.isEnabled = false
        viewBinding.chartEvents.setNoDataTextColor(resources.getColor(R.color.white, null))
        viewBinding.chartEvents.isScaleYEnabled = false
        viewBinding.chartEvents.isHighlightPerTapEnabled = false
        viewBinding.chartEvents.isHighlightPerDragEnabled = false
        viewBinding.chartEvents.legend.isEnabled = false

        val xAxis = viewBinding.chartEvents.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.gridColor = resources.getColor(gridColorID, null)
        xAxis.axisLineColor = resources.getColor(gridColorID, null)
        xAxis.textColor = resources.getColor(xAxisColorID, null)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)

        xAxis.granularity = 360000.0f

        val dateFormatter = SimpleDateFormat("hh:mm")
        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = dateFormatter.format(Date(value.toLong()))
        }

        xAxis.valueFormatter = formatter


        val rightAxis = viewBinding.chartEvents.axisRight
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

        val leftAxis = viewBinding.chartEvents.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID, null)


    }

    fun updateChart(eventList: List<SleepEventsViewData.Interval>) {
        val entries: ArrayList<BarEntry> = ArrayList()

        var startTime: Long = Long.MAX_VALUE
        var endTime: Long = Long.MIN_VALUE

        for (eventSet in eventList) {
            val values = floatArrayOf(eventSet.minorEvents.toFloat(), eventSet.majorEvents.toFloat())
            val entry = BarEntry(eventSet.startAt.toFloat(), values)
            entries.add(entry)

            if (eventSet.startAt < startTime) {
                startTime = eventSet.startAt
            }

            if (eventSet.startAt > endTime) {
                endTime = eventSet.startAt
            }

        }

        val barDataSet = BarDataSet(entries, "Events")
        barDataSet.setDrawValues(false)
        barDataSet.setDrawIcons(false)

        val barColors: ArrayList<Int> = ArrayList()
        barColors.add(resources.getColor(R.color.event_color_minor, null))
        barColors.add(resources.getColor(R.color.event_color_major, null))
        barDataSet.colors = barColors
        val barData = BarData(barDataSet)

        val boundaryEntryList: ArrayList<BarEntry> = ArrayList()
        boundaryEntryList.add(
            BarEntry(
                (startTime - 60 * 60 * 1800).toFloat(),
                floatArrayOf(0.toFloat())
            )
        )
        boundaryEntryList.add(
            BarEntry(
                (endTime + 60 * 60 * 1800).toFloat(),
                floatArrayOf(0.toFloat())
            )
        )
        val boundarySet = BarDataSet(boundaryEntryList, "")
        boundarySet.setDrawValues(false)
        boundarySet.setDrawIcons(false)
        val boundaryColors: ArrayList<Int> = ArrayList()
        boundaryColors.add(resources.getColor(R.color.clear, null))
        boundarySet.colors = boundaryColors

        barData.addDataSet(boundarySet)


        var width = 1800000.0f
        val tWidth = ((endTime - startTime) / 10.0).toFloat()
        if (tWidth < width) {
            width = tWidth
        }

        if (width == 0.0f) {
            width = 1800000.0f
        }

        barData.barWidth = width

        viewBinding.chartEvents.setFitBars(true)
        viewBinding.chartEvents.data = barData
        viewBinding.chartEvents.invalidate()
    }
}