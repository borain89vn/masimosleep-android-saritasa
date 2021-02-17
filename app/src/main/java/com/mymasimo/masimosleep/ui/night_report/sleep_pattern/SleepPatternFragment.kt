package com.mymasimo.masimosleep.ui.night_report.sleep_pattern

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.night_report.sleep_pattern.util.SleepPatternViewData
import com.mymasimo.masimosleep.util.calculateTimeOfDayToMinutes
import kotlinx.android.synthetic.main.fragment_sleep_pattern.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SleepPatternFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: SleepPatternViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        when {
            args.containsKey(PROGRAM_ID_KEY) -> {
                vm.onCreatedWithProgramId(args.getLong(PROGRAM_ID_KEY))
            }
            args.containsKey(SESSION_ID_KEY) -> {
                vm.onCreatedWithSessionId(args.getLong(SESSION_ID_KEY))
            }
            else                             -> throw IllegalStateException()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep_pattern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.viewData.observe(viewLifecycleOwner) { viewData ->
            Timber.d("ViewData: $viewData")
            updateUI(viewData)
        }
    }

    fun updateUI(viewData: SleepPatternViewData) {

        hideAllBars()

        val lowHour = viewData.lowMinutes / 60
        val lowMinutes = viewData.lowMinutes % 60

        var lowDurationString = lowHour.toString() + "h " + lowMinutes.toString() + "m"
        if (lowHour == 0) {
            lowDurationString = lowMinutes.toString() + "m"
        }

        val highHour = viewData.highMinutes / 60
        val highMinutes = viewData.highMinutes % 60

        var highDurationString = highHour.toString() + "h " + highMinutes.toString() + "m"
        if (highHour == 0) {
            highDurationString = highMinutes.toString() + "m"
        }

        low_high_text.text = "$lowDurationString - $highDurationString"

        val dateFormatter = SimpleDateFormat("hh:mm aa")

        val avgSleepTimeString = dateFormatter.format(Date(viewData.avgSleepStartAt))
        val avgWakeTimeString = dateFormatter.format(Date(viewData.avgSleepEndAt))

        val avgStartMinutes = calculateTimeOfDayToMinutes(viewData.avgSleepStartAt)

        avg_sleep_text.text = avgSleepTimeString
        avg_wake_text.text = avgWakeTimeString

        // Temporary change to avoid label messed up
        sleep_time_text.text = "0h"
        wake_time_text.text = highDurationString

        //calculate average night duration first
        var count: Int = 0
        var totalDuration: Double = 0.0
        for (session in viewData.sleepSessions) {

            val nightDuration: Double = (session.endAt - session.startAt).toDouble() / 1000.0
            totalDuration += nightDuration
            count++
        }

        val avgDuration: Double = totalDuration / count.toDouble()

        val avgHour = (avgDuration / 3600).toInt()
        val avgMinutes = (avgDuration % 3600 / 60).toInt()

        var avgDurationString = avgHour.toString() + "h " + avgMinutes.toString() + "m"
        if (lowHour == 0) {
            avgDurationString = avgMinutes.toString() + "m"
        }

        avg_duration_text.text = avgDurationString

        val screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val fullWidth = screenWidth - 170 * density
        val zero = 38 * density

        val longestSession = viewData.sleepSessions.maxBy { it.endAt - it.startAt }

        // Temporary change to avoid label and bars messed up
        longestSession?.let { longestSession ->
            for (session in viewData.sleepSessions) {

                val nightDuration: Double = (session.endAt - session.startAt).toDouble()
                val lengthPercentage = nightDuration / (longestSession.endAt - longestSession.startAt)
                //            val lengthPercentage = nightDuration / avgDuration

                //            val startMinutes = calculateTimeOfDayToMinutes(session.startAt)
                //            val ratio: Double = startMinutes.toDouble() / avgStartMinutes.toDouble()
                //            val startPercent: Double = ratio - 1.0
                val startPercent: Double = 0.0

                //Timber.d("night: ${session.night}, ratio: $ratio, startPercent: $startPercent, lengthPercent: $lengthPercentage")

                getViewForNight(session.night)?.let { percentView ->
                    percentView.visibility = View.VISIBLE

                    val params = ConstraintLayout.LayoutParams(
                            (fullWidth * lengthPercentage).toInt(),
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val start = (zero + zero * startPercent).toInt()
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    params.setMargins(start, 0, 0, 0)
                    percentView.layoutParams = params

                }

            }
        }

    }

    fun hideAllBars() {
        num1_view.visibility = View.INVISIBLE
        num2_view.visibility = View.INVISIBLE
        num3_view.visibility = View.INVISIBLE
        num4_view.visibility = View.INVISIBLE
        num5_view.visibility = View.INVISIBLE
        num6_view.visibility = View.INVISIBLE
        num7_view.visibility = View.INVISIBLE
        num8_view.visibility = View.INVISIBLE
        num9_view.visibility = View.INVISIBLE
        num10_view.visibility = View.INVISIBLE

    }

    private fun getViewForNight(night: Int) = when (night) {
        1    -> num1_view
        2    -> num2_view
        3    -> num3_view
        4    -> num4_view
        5    -> num5_view
        6    -> num6_view
        7    -> num7_view
        else -> throw IllegalArgumentException("nights must not be larger than $NUM_OF_NIGHTS")
    }

    companion object {
        private const val SESSION_ID_KEY = "SESSION_ID"
        private const val PROGRAM_ID_KEY = "PROGRAM_ID"

        fun newInstanceWithSessionId(sessionId: Long) = SleepPatternFragment().apply {
            arguments = bundleOf(SESSION_ID_KEY to sessionId)
        }

        fun newInstanceWithProgramId(programId: Long) = SleepPatternFragment().apply {
            arguments = bundleOf(PROGRAM_ID_KEY to programId)
        }
    }
}