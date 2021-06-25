package com.mymasimo.masimosleep.ui.night_report.report_events

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentReportEventsBinding
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import com.mymasimo.masimosleep.ui.night_report.report_events.util.SleepEventsViewData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ReportEventsFragment : Fragment(R.layout.fragment_report_events) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportEventsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportEventsBinding::bind)

    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        vm.onCreated(sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.sleepEvents.observe(viewLifecycleOwner) { viewData ->
            receivedEventsConfiguration()
            updateUI(viewData)
        }

        viewBinding.noEventsText.text = getString(R.string.day_events_empty, MasimoSleepPreferences.name)
        noEventsConfiguration()

        viewBinding.viewEventsButton.setOnClickListener {
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToEventDetailsFragment(
                    sessionId
                )
            )
        }
    }

    private fun noEventsConfiguration() {
        viewBinding.noEventsTray.visibility = View.VISIBLE
        viewBinding.eventTray.visibility = View.GONE
        viewBinding.chartEvents.visibility = View.GONE
        viewBinding.viewEventsButton.visibility = View.GONE
    }

    private fun receivedEventsConfiguration() {
        configureChart()
        viewBinding.noEventsTray.visibility = View.GONE
        viewBinding.eventTray.visibility = View.VISIBLE
        viewBinding.chartEvents.visibility = View.VISIBLE
        viewBinding.viewEventsButton.visibility = View.VISIBLE
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

    private fun configureChart() {
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
        xAxis.setLabelCount(5, true)

        xAxis.granularity = 60000.0f

        val dateFormatter = SimpleDateFormat("hh:mm")
        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = dateFormatter.format(Date(value.toLong().toMillis()))
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
        rightAxis.spaceBottom = 0.0f
        rightAxis.yOffset = -9F
        rightAxis.gridColor = resources.getColor(gridColorID, null)
        rightAxis.textColor = resources.getColor(yAxisColorID, null)

        val leftAxis = viewBinding.chartEvents.axisLeft
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisLineColor = resources.getColor(gridColorID, null)
    }

    private fun Long.toMinutes() = this / 1000 / 60
    private fun Long.toMillis() = this * 1000 * 60

    private fun updateChart(eventList: List<SleepEventsViewData.Interval>) {
        val entries: ArrayList<BarEntry> = ArrayList()

        var startTime: Long = Long.MAX_VALUE.toMinutes()
        var endTime: Long = Long.MIN_VALUE.toMillis()

        for (eventSet in eventList) {

            val values = floatArrayOf(eventSet.minorEvents.toFloat(), eventSet.majorEvents.toFloat())
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

        val barDataSet = BarDataSet(entries, resources.getString(R.string.sleep_events))
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
                (startTime - 30).toFloat(),
                floatArrayOf(0.toFloat())
            )
        )
        boundaryEntryList.add(
            BarEntry(
                (endTime + 30).toFloat(),
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

        barData.barWidth = FIFTEEN_MINUTES.toFloat()

        viewBinding.chartEvents.setFitBars(true)
        viewBinding.chartEvents.data = barData
        viewBinding.chartEvents.invalidate()
    }

    companion object {
        private const val gridColorID: Int = R.color.chart_grid_light
        private const val xAxisColorID: Int = R.color.chart_x_label_light
        private const val yAxisColorID: Int = R.color.chart_y_label_light

        private const val KEY_SESSION_ID = "SESSION_ID"

        private const val FIFTEEN_MINUTES = 15

        fun newInstance(sessionId: Long) = ReportEventsFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}