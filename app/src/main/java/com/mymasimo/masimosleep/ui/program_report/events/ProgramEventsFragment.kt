package com.mymasimo.masimosleep.ui.program_report.events

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.night_report.report_events.util.SleepEventsViewData
import kotlinx.android.synthetic.main.fragment_program_events.*
import timber.log.Timber

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ProgramEventsFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramEventsViewModel by viewModels { vmFactory }

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_program_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.eventsViewData.observe(viewLifecycleOwner) { viewData ->
            receivedEventsConfiguration()
            updateUI(viewData)
        }

        no_events_text.text = "Everything went great!\nEnjoy your day, " + MasimoSleepPreferences.name + "."
        noEventsConfiguration()

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

    private fun updateUI(eventsData : ProgramEventsViewModel.ProgramEventsViewData) {
        val totalEvents = eventsData.totalEvents

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

        minor_event_text.text = eventsData.minorEvents.toString()
        major_event_text.text = eventsData.majorEvents.toString()

        updateChart(eventsData.eventsByNight)
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
        xAxis.setCenterAxisLabels(false)
        xAxis.granularity = 1.0f
        xAxis.labelCount = NUM_OF_NIGHTS


        val formatter = object :  ValueFormatter() {
            override fun getFormattedValue(value: Float): String {

                return value.toInt().toString()
            }
        }

        //xAxis.valueFormatter = formatter

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
        rightAxis.gridColor = resources.getColor(gridColorID,null)
        rightAxis.textColor = resources.getColor(yAxisColorID,null)

        val leftAxis = chart_events.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID,null)
    }

    private fun updateChart(eventList : List<ProgramEventsViewModel.ProgramEventsViewData.Night>) {
        val entries : ArrayList<BarEntry> = ArrayList()

        for (eventSet in eventList) {
            val night = eventSet.index
            val values = floatArrayOf(eventSet.minorEvents.toFloat(),eventSet.majorEvents.toFloat())
            val entry = BarEntry(night.toFloat(), values)
            entries.add(entry)
        }

        val barDataSet = BarDataSet(entries,"Events")
        barDataSet.setDrawValues(false)
        barDataSet.setDrawIcons(false)

        var barColors : ArrayList<Int> = ArrayList()
        barColors.add(resources.getColor(R.color.event_color_minor,null))
        barColors.add(resources.getColor(R.color.event_color_major,null))
        barDataSet.colors = barColors
        val barData = BarData(barDataSet)
        barData.barWidth = 0.75f

        val boundaryEntryList : ArrayList<BarEntry> = ArrayList()
        boundaryEntryList.add(
            BarEntry(
                1.0f,
                floatArrayOf(0.toFloat()))
        )
        boundaryEntryList.add(
            BarEntry(
                NUM_OF_NIGHTS.toFloat(),
                floatArrayOf(0.toFloat()))
        )

        val boundarySet = BarDataSet(boundaryEntryList,"")
        boundarySet.setDrawValues(false)
        boundarySet.setDrawIcons(false)
        var boundaryColors : ArrayList<Int> = ArrayList()
        boundaryColors.add(resources.getColor(R.color.clear,null))
        boundarySet.colors = boundaryColors

        barData.addDataSet(boundarySet)

        chart_events.setFitBars(true)
        chart_events.data = barData
        chart_events.invalidate()
    }

    companion object {

        private val gridColorID : Int = R.color.chart_grid_light
        private val xAxisColorID : Int = R.color.chart_x_label_light
        private val yAxisColorID : Int = R.color.chart_y_label_light


        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = ProgramEventsFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }
}