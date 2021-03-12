package com.mymasimo.masimosleep.ui

import android.R
import android.content.Context
import android.util.AttributeSet
import android.widget.NumberPicker
import android.widget.TimePicker
import java.lang.reflect.Field
import java.text.DecimalFormat

const val minuteInterval = 15

class CustomTimePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.timePickerStyle) :
    TimePicker(context, attrs, defStyleAttr) {

    override fun getMinute(): Int {
        return mCurrentMinute
    }

    override fun setMinute(minute: Int) {
        super.setMinute(mCurrentMinute)
        updateMinuteSpinner(minute)
    }

    private fun updateMinuteSpinner(minute: Int) {
        try {
            val classForid = Class.forName("com.android.internal.R\$id")
            val field: Field = classForid.getField("minute")
            val minuteSpinner = findViewById<NumberPicker>(field.getInt(null))

            if (minuteSpinner != null) {
                minuteSpinner.value = minute / minuteInterval
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var mInitialMinute = 0
    private var mCurrentMinute = 0

    init {
        val numValues = 60 / minuteInterval
        val displayedValues = arrayOfNulls<String>(numValues)
        val formatter = DecimalFormat("00")
        for (i in 0 until numValues) {
            displayedValues[i] = formatter.format(i * minuteInterval)
        }
        try {
            val classForid = Class.forName("com.android.internal.R\$id")
            val field: Field = classForid.getField("minute")
            val minuteSpinner = findViewById<NumberPicker>(field.getInt(null))
            if (minuteSpinner != null) {
                minuteSpinner.minValue = 0
                minuteSpinner.maxValue = numValues - 1
                minuteSpinner.displayedValues = displayedValues
                minuteSpinner.setOnValueChangedListener { picker, oldVal, newVal -> mCurrentMinute = newVal * minuteInterval }
                val initPos: Int = mInitialMinute / minuteInterval
                minuteSpinner.value = initPos
                mCurrentMinute = initPos * minuteInterval
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}