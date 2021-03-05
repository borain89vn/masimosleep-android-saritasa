package com.mymasimo.masimosleep.ui.session.session_events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
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
import com.mymasimo.masimosleep.ui.session.session_events.util.SleepEventsViewData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_session_events.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class SessionEventsFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: SessionSleepEventsViewModel by viewModels { vmFactory }

    companion object {
        private const val START_TIME_KEY = "START_TIME"

        private val gridColorID: Int = R.color.chart_grid_dark
        private val xAxisColorID: Int = R.color.chart_x_label_dark
        private val yAxisColorID: Int = R.color.chart_y_label_dark

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
            startTime = getLong(SessionEventsFragment.START_TIME_KEY)
        }

        vm.onCreate(startTime)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_session_events, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        no_events_text.text = getString(R.string.sleep_events_empty, MasimoSleepPreferences.name)
        noEventsConfiguration()

        vm.sleepEvents.observe(viewLifecycleOwner) { sleepEventData ->
            updateUI(sleepEventData)
        }

    }

    private fun noEventsConfiguration() {
        no_events_tray.visibility = View.VISIBLE
        event_tray.visibility = View.GONE
        chart_events.visibility = View.GONE
    }

    private fun receivedEventsConfiguration() {
        configureChart()
        no_events_tray.visibility = View.GONE
        event_tray.visibility = View.VISIBLE
        chart_events.visibility = View.VISIBLE
    }

    private fun updateUI(sleepEventData: SleepEventsViewData) {
        val totalEvents = sleepEventData.totalEvents

        if (totalEvents == 0) {
            noEventsConfiguration()
        } else {
            receivedEventsConfiguration()
        }

        event_text.text = resources.getQuantityString(R.plurals.events_occurred, totalEvents, totalEvents)

        minor_event_text.text = sleepEventData.minorEvents.toString()
        major_event_text.text = sleepEventData.majorEvents.toString()

        updateChart(sleepEventData.eventsByHour)
    }

    fun configureChart() {
        chart_events.description.isEnabled = false
        chart_events.setNoDataTextColor(resources.getColor(R.color.white, null))
        chart_events.isScaleYEnabled = false
        chart_events.isHighlightPerTapEnabled = false
        chart_events.isHighlightPerDragEnabled = false
        chart_events.legend.isEnabled = false

        val xAxis = chart_events.xAxis
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


        val rightAxis = chart_events.axisRight
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

        val leftAxis = chart_events.axisLeft
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

        chart_events.setFitBars(true)
        chart_events.data = barData
        chart_events.invalidate()
    }
}