package com.mymasimo.masimosleep.ui.home.night_picker

import android.content.Context
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

class NightButtonView(context: Context, night: Int, state: NightButtonState, date: DateOfWeek? = null) : ConstraintLayout(context) {

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

        when (state) {
            NightButtonState.PAST -> {
                visibility = View.VISIBLE
                enabled = true
                buttonBG = R.drawable.night_button_past
                buttonTextColor = R.color.white
                selected = false
                viewBinding.nightButton.setOnClickListener {
                    listener()
                }
            }
            NightButtonState.PAST_SELECTED -> {
                visibility = View.VISIBLE
                enabled = true
                buttonBG = R.drawable.night_button_past
                buttonTextColor = R.color.white
                selected = true
            }
            NightButtonState.PRESENT -> {
                visibility = View.VISIBLE
                enabled = true
                buttonBG = R.drawable.night_button_present
                buttonTextColor = R.color.white
                selected = false
                viewBinding.nightButton.setOnClickListener {
                    listener()
                }
            }
            NightButtonState.PRESENT_SELECTED -> {
                visibility = View.VISIBLE
                enabled = true
                buttonBG = R.drawable.night_button_present
                buttonTextColor = R.color.white
                selected = true
                viewBinding.nightButton.setOnClickListener {
                    listener()
                }
            }
            NightButtonState.BLANK -> {
                visibility = View.INVISIBLE
                enabled = false
            }
        }

        this.visibility = visibility
        this.isEnabled = enabled
        viewBinding.nightButton.background = ResourcesCompat.getDrawable(resources, buttonBG, null)
        viewBinding.nightButton.isSelected = selected
        setTextStyle(state,resources.getColor(buttonTextColor, context.theme),date)

        //automation
        contentDescription = "$night"


    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun setTextStyle(state: NightButtonState, color: Int, date: DateOfWeek?) {
        val button = viewBinding.nightButton
        if (state == NightButtonState.BLANK) {
            return
        } else if (state == NightButtonState.PRESENT_SELECTED || state == NightButtonState.PRESENT) {
            button.text = context.getString(R.string.today_label_btn)
            button.setTextColor(color)
            return
        }
        date?.let {
            val dayOfWeek = "${date.dayOfWeek}\n"
            val dayOfMonth = date.days
            val text = "$dayOfWeek$dayOfMonth"
            val spanString = SpannableString(text)

            button.setTextColor(color)
            spanString.setSpan(
                StyleSpan(Typeface.BOLD),
                dayOfWeek.length,
                text.length,
                SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spanString.setSpan(
                RelativeSizeSpan(1.3f),
                dayOfWeek.length,
                text.length,
                SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            button.text = spanString
        }
    }
}