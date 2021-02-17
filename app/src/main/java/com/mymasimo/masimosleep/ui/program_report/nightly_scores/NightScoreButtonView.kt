package com.mymasimo.masimosleep.ui.program_report.nightly_scores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.night_score_button_view.view.*


class NightScoreButtonView(context: Context, night : Int, score : Int, state : NightScoreButtonState) : ConstraintLayout(context) {

    val view : View
    private val state : NightScoreButtonState
    private lateinit var listener: () -> Unit

    init {
        this.view = LayoutInflater.from(context).inflate(R.layout.night_score_button_view, this, true)

        val screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val buttonsPerRow: Int = 5
        val availableWidth = screenWidth - 64*density

        val params = LayoutParams(availableWidth.toInt() / buttonsPerRow, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(2*density.toInt(),0,2*density.toInt(),0)
        this.view.layoutParams = params


        this.state = state

        if (state == NightScoreButtonState.FUTURE) {

            night_button.isEnabled = false
            night_button.setPadding(0,0,0,0)

            dot_view.visibility = View.GONE
            number_text.visibility = View.GONE

        } else if (state == NightScoreButtonState.PAST) {

            night_button.isEnabled = true
            night_button.setPadding(0 ,  0,   0, 24*density.toInt())

            dot_view.visibility = View.VISIBLE
            number_text.visibility = View.VISIBLE

            night_button.setOnClickListener {
                this.listener()
            }
        }

        night_button.text = "Night $night"
        number_text.text = "$score"

        var dotColorID : Int = R.drawable.red_dot
        if (score < resources.getInteger(R.integer.red_upper)) {
            dotColorID = R.drawable.red_dot
        } else if (score <= resources.getInteger(R.integer.yellow_upper)) {
            dotColorID = R.drawable.yellow_dot
        } else if (score > resources.getInteger(R.integer.yellow_upper)) {
            dotColorID = R.drawable.green_dot
        }

        dot_view.background = resources.getDrawable(dotColorID,null)

        //automation
        this.view.contentDescription = "$night"

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}