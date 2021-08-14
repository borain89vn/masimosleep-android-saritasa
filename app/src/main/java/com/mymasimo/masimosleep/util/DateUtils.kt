package com.mymasimo.masimosleep.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

object DateUtils {

    fun getNextDays(startdate: Long, days: Int): MutableList<LocalDate> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startdate
        val fmt = SimpleDateFormat("yyyy-MM-dd")
        val localDates: MutableList<LocalDate> = ArrayList()
        try {
            val localDate: LocalDate = LocalDate.parse(fmt.format(calendar.time))
            localDates.add(localDate)
            for (i in 1..days) {
                localDates.add(localDate.plusDays(i.toLong()))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return localDates
    }
}

data class DateOfWeek(var days: Int, var dayOfWeek: String)
