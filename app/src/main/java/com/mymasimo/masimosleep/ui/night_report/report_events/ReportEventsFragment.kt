package com.mymasimo.masimosleep.ui.night_report.report_events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import com.mymasimo.masimosleep.ui.night_report.report_events.util.SleepEventsViewData
import kotlinx.android.synthetic.main.fragment_report_events.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ReportEventsFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportEventsViewModel by viewModels { vmFactory }

    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        vm.onCreated(sessionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.sleepEvents.observe(viewLifecycleOwner) { viewData ->
            receivedEventsConfiguration()
            updateUI(viewData)
        }

        no_events_text.text = "Everything looks great!\nEnjoy your day, " + MasimoSleepPreferences.name + "."
        noEventsConfiguration()

        view_events_button.setOnClickListener{
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToEventDetailsFragment(
                    sessionId
                )
            )
        }
    }

    private fun noEventsConfiguration() {
        no_events_tray.visibility = View.VISIBLE
        event_tray.visibility = View.GONE
        chart_events.visibility = View.GONE
        view_events_button.visibility = View.GONE
    }

    private fun receivedEventsConfiguration() {
        configureChart()
        no_events_tray.visibility = View.GONE
        event_tray.visibility = View.VISIBLE
        chart_events.visibility = View.VISIBLE
        view_events_button.visibility = View.VISIBLE
    }

    private fun updateUI(sleepEventData : SleepEventsViewData) {
        val totalEvents = sleepEventData.totalEvents

        if (totalEvents == 0) {
            noEventsConfiguration()
        } else {
            receivedEventsConfiguration()
        }

        var plural = "s"
        if (totalEvents == 1) {
            plural = ""
        }
        event_text.text =  totalEvents.toString() + " Event$plural occured"

        minor_event_text.text = sleepEventData.minorEvents.toString()
        major_event_text.text = sleepEventData.majorEvents.toString()

        updateChart(sleepEventData.eventsByHour)
    }

    private fun configureChart() {
        chart_events.description.isEnabled = false
        chart_events.setNoDataTextColor(resources.getColor(R.color.white,null))
        chart_events.isScaleYEnabled = false
        chart_events.isHighlightPerTapEnabled = false
        chart_events.isHighlightPerDragEnabled = false
        chart_events.legend.isEnabled = false

        val xAxis = chart_events.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.gridColor = resources.getColor(gridColorID,null)
        xAxis.axisLineColor = resources.getColor(gridColorID,null)
        xAxis.textColor = resources.getColor(xAxisColorID,null)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)
        xAxis.setLabelCount(5, true)

        xAxis.granularity = 60000.0f

        val dateFormatter = SimpleDateFormat("hh:mm")
        val formatter = object :  ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val date = dateFormatter.format(Date(value.toLong().toMillis()))

                return date
            }
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
        rightAxis.spaceBottom = 0.0f
        rightAxis.yOffset = -9F
        rightAxis.gridColor = resources.getColor(gridColorID,null)
        rightAxis.textColor = resources.getColor(yAxisColorID,null)

        val leftAxis = chart_events.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID,null)
    }

    private fun Long.toMinutes() = this / 1000 / 60
    private fun Long.toMillis() = this * 1000 * 60

    private fun updateChart(eventList : List<SleepEventsViewData.Interval>) {
        val entries : ArrayList<BarEntry> = ArrayList()

        var startTime : Long = Long.MAX_VALUE.toMinutes()
        var endTime : Long = Long.MIN_VALUE.toMillis()

        for (eventSet in eventList) {

            val values = floatArrayOf(eventSet.minorEvents.toFloat(),eventSet.majorEvents.toFloat())
            val entry = BarEntry(eventSet.startAt.toMinutes().toFloat() + 7.5f, values)
            entries.add(entry)

            val eventStartInMinutes = eventSet.startAt.toMinutes()

            if (eventStartInMinutes < startTime) {
                startTime = eventStartInMinutes
            }

            if (eventStartInMinutes > endTime) {
                endTime = eventStartInMinutes
            }
        }

        val barDataSet = BarDataSet(entries,"Events")
        barDataSet.setDrawValues(false)
        barDataSet.setDrawIcons(false)

        val barColors : ArrayList<Int> = ArrayList()
        barColors.add(resources.getColor(R.color.event_color_minor,null))
        barColors.add(resources.getColor(R.color.event_color_major,null))
        barDataSet.colors = barColors
        val barData = BarData(barDataSet)

        val boundaryEntryList : ArrayList<BarEntry> = ArrayList()
        boundaryEntryList.add(
            BarEntry(
                (startTime - 30).toFloat(),
                floatArrayOf(0.toFloat()))
        )
        boundaryEntryList.add(
            BarEntry(
            (endTime + 30).toFloat(),
                floatArrayOf(0.toFloat()))
        )

        val boundarySet = BarDataSet(boundaryEntryList,"")
        boundarySet.setDrawValues(false)
        boundarySet.setDrawIcons(false)
        var boundaryColors : ArrayList<Int> = ArrayList()
        boundaryColors.add(resources.getColor(R.color.clear,null))
        boundarySet.colors = boundaryColors

        barData.addDataSet(boundarySet)

        barData.barWidth = FIFTEEN_MINUTES.toFloat()

        chart_events.setFitBars(true)
        chart_events.data = barData
        chart_events.invalidate()
    }

    companion object {
        private val gridColorID : Int = R.color.chart_grid_light
        private val xAxisColorID : Int = R.color.chart_x_label_light
        private val yAxisColorID : Int = R.color.chart_y_label_light

        private const val KEY_SESSION_ID = "SESSION_ID"

        private const val FIFTEEN_MINUTES = 15

        fun newInstance(sessionId: Long) = ReportEventsFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}