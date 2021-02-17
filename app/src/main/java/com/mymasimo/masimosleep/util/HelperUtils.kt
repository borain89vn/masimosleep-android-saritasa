package com.mymasimo.masimosleep.util

import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import com.mymasimo.masimosleep.BuildConfig
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import java.text.SimpleDateFormat
import java.util.*

fun getMinChartValue(type: ReadingType = ReadingType.DEFAULT): Int {

    return when (type) {
        ReadingType.SP02 -> 50

        ReadingType.PR -> 20

        ReadingType.RRP -> 4

        ReadingType.DEFAULT -> 0
    }
}

fun getMaxChartValue(type: ReadingType = ReadingType.DEFAULT): Int {

    return when (type) {
        ReadingType.SP02 -> 100

        ReadingType.PR -> 160

        ReadingType.RRP -> 40

        ReadingType.DEFAULT -> 100
    }
}

fun getChartColorForValue(context: Context, type: ReadingType, value: Double): Int {

    val resources = context.resources

    return when (type) {

        ReadingType.SP02 -> {
            getChartColor(
                    value,
                    resources.getInteger(R.integer.spo2_optimal_low),
                    resources.getInteger(R.integer.spo2_optimal)
            )
        }

        ReadingType.PR -> {

            getChartColor(
                    value,
                    resources.getInteger(R.integer.pr_optimal_low),
                    resources.getInteger(R.integer.pr_optimal),
                    resources.getInteger(R.integer.pr_optimal_high),
                    resources.getInteger(R.integer.pr_high)
            )
        }

        ReadingType.RRP -> {

            getChartColor(
                    value,
                    resources.getInteger(R.integer.rrp_optimal_low),
                    resources.getInteger(R.integer.rrp_optimal),
                    resources.getInteger(R.integer.rrp_optimal_high),
                    resources.getInteger(R.integer.rrp_high)
            )
        }

        else -> R.color.black
    }
}

fun getChartColor(value: Double,
                  rOptimalLow: Int,
                  rOptimal: Int): Int {

    var colorID: Int = R.color.black

    val valueInt = value.toInt()

    if (valueInt < rOptimalLow) {
        colorID = R.color.low
    } else if (valueInt < rOptimal) {
        colorID = R.color.optimal_low
    } else if (valueInt >= rOptimal) {
        colorID = R.color.optimal
    }


    return colorID
}

fun getChartColor(value: Double,
                  rOptimalLow: Int,
                  rOptimal: Int,
                  rOptimalHigh: Int,
                  rHigh: Int): Int {

    var colorID: Int = R.color.black

    val valueInt = value.toInt()

    if (valueInt < rOptimalLow) {
        colorID = R.color.low
    } else if (valueInt < rOptimal) {
        colorID = R.color.optimal_low
    } else if (valueInt < rOptimalHigh) {
        colorID = R.color.optimal
    } else if (valueInt < rHigh) {
        colorID = R.color.optimal_high
    } else if (valueInt >= rHigh) {
        colorID = R.color.high
    }

    return colorID
}

fun calculateTimeOfDayToMinutes(value: Long): Int {

    val hourFormatter = SimpleDateFormat("H")
    val minuteFormatter = SimpleDateFormat("m")

    val hours = hourFormatter.format(Date(value)).toInt()
    val minutes = minuteFormatter.format(Date(value)).toInt()

    var totalMinutes = hours * 60 + minutes

    //count up to 4am as same day
    if (totalMinutes < 240) {
        totalMinutes += 1440
    }

    return totalMinutes
}

fun getRawMp4URI(@RawRes mp4Res: Int): Uri {
    val path = "android.resource://" + BuildConfig.APPLICATION_ID + "/" + mp4Res
    return Uri.parse(path)
}