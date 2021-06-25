package com.mymasimo.masimosleep.ui.program_report.events

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
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProgramEventsBinding
import java.util.*
import javax.inject.Inject


class ProgramEventsFragment : Fragment(R.layout.fragment_program_events) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ProgramEventsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentProgramEventsBinding::bind)

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.eventsViewData.observe(viewLifecycleOwner) { viewData ->
            receivedEventsConfiguration()
            updateUI(viewData)
        }

        viewBinding.noEventsText.text = getString(R.string.day_events_empty, MasimoSleepPreferences.name)
        noEventsConfiguration()
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

    private fun updateUI(eventsData: ProgramEventsViewModel.ProgramEventsViewData) {
        val totalEvents = eventsData.totalEvents

        if (totalEvents == 0) {
            noEventsConfiguration()
        } else {
            receivedEventsConfiguration()
        }

        viewBinding.eventText.text = resources.getQuantityString(R.plurals.events_occurred, totalEvents, totalEvents)

        viewBinding.minorEventText.text = eventsData.minorEvents.toString()
        viewBinding.majorEventText.text = eventsData.majorEvents.toString()

        updateChart(eventsData.eventsByNight)
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
        xAxis.setCenterAxisLabels(false)
        xAxis.granularity = 1.0f
        xAxis.labelCount = NUM_OF_NIGHTS


        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        //xAxis.valueFormatter = formatter

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

    private fun updateChart(eventList: List<ProgramEventsViewModel.ProgramEventsViewData.Night>) {
        val entries: ArrayList<BarEntry> = ArrayList()

        for (eventSet in eventList) {
            val night = eventSet.index
            val values = floatArrayOf(eventSet.minorEvents.toFloat(), eventSet.majorEvents.toFloat())
            val entry = BarEntry(night.toFloat(), values)
            entries.add(entry)
        }

        val barDataSet = BarDataSet(entries, resources.getString(R.string.sleep_events))
        barDataSet.setDrawValues(false)
        barDataSet.setDrawIcons(false)

        val barColors: ArrayList<Int> = ArrayList()
        barColors.add(resources.getColor(R.color.event_color_minor, null))
        barColors.add(resources.getColor(R.color.event_color_major, null))
        barDataSet.colors = barColors
        val barData = BarData(barDataSet)
        barData.barWidth = 0.75f

        val boundaryEntryList: ArrayList<BarEntry> = ArrayList()
        boundaryEntryList.add(
            BarEntry(
                1.0f,
                floatArrayOf(0.toFloat())
            )
        )
        boundaryEntryList.add(
            BarEntry(
                NUM_OF_NIGHTS.toFloat(),
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

        viewBinding.chartEvents.setFitBars(true)
        viewBinding.chartEvents.data = barData
        viewBinding.chartEvents.invalidate()
    }

    companion object {
        private const val gridColorID: Int = R.color.chart_grid_light
        private const val xAxisColorID: Int = R.color.chart_x_label_light
        private const val yAxisColorID: Int = R.color.chart_y_label_light

        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = ProgramEventsFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }
}