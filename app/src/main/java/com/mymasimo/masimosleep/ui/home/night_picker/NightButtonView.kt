package com.mymasimo.masimosleep.ui.home.night_picker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.night_button_view.view.*

class NightButtonView(context: Context, night: Int, state: NightButtonState) : ConstraintLayout(context) {

    val view: View = LayoutInflater.from(context).inflate(R.layout.night_button_view, this, true)
    private val state: NightButtonState
    private lateinit var listener: () -> Unit

    init {

        val screenWidth = resources.displayMetrics.widthPixels
        val buttonsPerRow = 5
        this.view.layoutParams = ViewGroup.LayoutParams(screenWidth / buttonsPerRow, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.state = state

        //default values are for future

        var visibility: Int = View.VISIBLE
        var enabled = false
        var buttonBG: Int = R.drawable.night_button_future
        var buttonTextColor: Int = R.color.secondaryText_light
        var selected = false
        var buttonText = context.getString(R.string.night_break_label_btn, night)

        if (state == NightButtonState.PAST) {
            visibility = View.VISIBLE
            enabled = true
            buttonBG = R.drawable.night_button_past
            buttonTextColor = R.color.black
            selected = false

            this.view.night_button.setOnClickListener {
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
            selected = true
            buttonText = context.getString(R.string.today_label_btn)

            this.view.night_button.setOnClickListener {
                listener()
            }

        } else if (state == NightButtonState.BLANK) {
            visibility = View.INVISIBLE
            enabled = false
        }

        this.view.visibility = visibility
        this.view.isEnabled = enabled

        this.view.night_button.background = resources.getDrawable(buttonBG, null)
        this.view.night_button.setTextColor(resources.getColor(buttonTextColor, null))
        this.view.night_button.isSelected = selected
        this.view.night_button.text = buttonText

        //automation
        this.view.contentDescription = "$night"

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

}