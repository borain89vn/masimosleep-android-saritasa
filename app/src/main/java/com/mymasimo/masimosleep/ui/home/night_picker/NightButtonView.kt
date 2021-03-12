package com.mymasimo.masimosleep.ui.home.night_picker

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.NightButtonViewBinding

class NightButtonView(context: Context, night: Int, state: NightButtonState) : ConstraintLayout(context) {

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
        var buttonTextColor: Int = R.color.secondaryText_light
        var selected = false
        var buttonText = context.getString(R.string.night_break_label_btn, night)

        if (state == NightButtonState.PAST) {
            visibility = View.VISIBLE
            enabled = true
            buttonBG = R.drawable.night_button_past
            buttonTextColor = R.color.black
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
            selected = true
            buttonText = context.getString(R.string.today_label_btn)

            viewBinding.nightButton.setOnClickListener {
                listener()
            }

        } else if (state == NightButtonState.BLANK) {
            visibility = View.INVISIBLE
            enabled = false
        }

        this.visibility = visibility
        this.isEnabled = enabled

        viewBinding.nightButton.background = ResourcesCompat.getDrawable(resources, buttonBG, null)
        viewBinding.nightButton.setTextColor(resources.getColor(buttonTextColor, null))
        viewBinding.nightButton.isSelected = selected
        viewBinding.nightButton.text = buttonText

        //automation
        contentDescription = "$night"

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}