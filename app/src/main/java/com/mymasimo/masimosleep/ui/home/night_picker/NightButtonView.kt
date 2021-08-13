package com.mymasimo.masimosleep.ui.home.night_picker

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.NightButtonViewBinding
import com.mymasimo.masimosleep.util.DateOfWeek
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.*
import android.widget.Button


class NightButtonView(context: Context, night: Int, state: NightButtonState,date:DateOfWeek?=null) : ConstraintLayout(context) {

    private val state: NightButtonState
    private lateinit var listener: () -> Unit
    private val viewBinding by viewBinding(NightButtonViewBinding::bind)

    init {
        inflate(context, R.layout.night_button_view, this)
        val screenWidth = resources.displayMetrics.widthPixels
        val buttonsPerRow = 5
        this.layoutParams = ViewGroup.LayoutParams(screenWidth / buttonsPerRow, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.state = state

        //default values are for future

        var visibility: Int = View.VISIBLE
        var enabled = false
        var buttonBG: Int = R.drawable.night_button_future
        var buttonTextColor: Int = R.color.home_night_future
        var selected = false



        if (state == NightButtonState.PAST) {
            visibility = View.VISIBLE
            enabled = true
            buttonBG = R.drawable.night_button_past
            buttonTextColor = R.color.white
            selected = false

            viewBinding.nightButton.setOnClickListener {
                listener()
            }
        } else if (state == NightButtonState.PAST_SELECTED) {
            visibility = View.VISIBLE
            enabled = true
            buttonBG = R.drawable.night_button_past
            buttonTextColor = R.color.white
            selected = true
        } else if (state == NightButtonState.PRESENT) {
            visibility = View.VISIBLE
            enabled = true
            buttonBG = R.drawable.night_button_present
            buttonTextColor = R.color.white
            selected = false

            viewBinding.nightButton.setOnClickListener {
                listener()
            }

        }else if (state == NightButtonState.PRESENT_SELECTED) {
            visibility = View.VISIBLE
            enabled = true
            buttonBG = R.drawable.night_button_present
            buttonTextColor = R.color.white
            selected = true

            viewBinding.nightButton.setOnClickListener {
                listener()
            }

        }
        else if (state == NightButtonState.BLANK) {
            visibility = View.INVISIBLE
            enabled = false
        }

        this.visibility = visibility
        this.isEnabled = enabled

        viewBinding.nightButton.background = ResourcesCompat.getDrawable(resources, buttonBG, null)
        viewBinding.nightButton.isSelected = selected
        setTextStyle(state,resources.getColor(buttonTextColor, null),date)

        //automation
        contentDescription = "$night"


    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun setTextStyle(state: NightButtonState,color: Int,date: DateOfWeek?){
        val button = viewBinding.nightButton
         if(state==NightButtonState.BLANK){
             return
         }else if(state ==NightButtonState.PRESENT_SELECTED|| state == NightButtonState.PRESENT){
             button.text = context.getString(R.string.today_label_btn)
             button.setTextColor(color)
             return
         }
         date?.let {
             val s1 = "${date.dayOfWeek}\n"
             val s2 = date.days
             val s = "$s1$s2"
             val ss = SpannableString(s)
             button.setTextColor(color)
             ss.setSpan(
                 StyleSpan(Typeface.BOLD),
                 s1.length,
                 s.length,
                 SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE
             )
             ss.setSpan(
                 RelativeSizeSpan(1.3f),
                 s1.length,
                 s.length,
                 SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE
             )

             button.text = ss
         }
    }


}