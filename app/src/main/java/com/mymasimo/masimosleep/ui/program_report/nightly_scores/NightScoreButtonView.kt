package com.mymasimo.masimosleep.ui.program_report.nightly_scores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.night_score_button_view.view.*


class NightScoreButtonView(context: Context, night: Int, score: Int, state: NightScoreButtonState) : ConstraintLayout(context) {

    val view: View = LayoutInflater.from(context).inflate(R.layout.night_score_button_view, this, true)
    private val state: NightScoreButtonState
    private lateinit var listener: () -> Unit

    init {
        val screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val buttonsPerRow = 5
        val availableWidth = screenWidth - 64 * density

        val params = LayoutParams(availableWidth.toInt() / buttonsPerRow, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(2 * density.toInt(), 0, 2 * density.toInt(), 0)
        this.view.layoutParams = params


        this.state = state

        if (state == NightScoreButtonState.FUTURE) {

            night_button.isEnabled = false
            night_button.setPadding(0, 0, 0, 0)

            dot_view.visibility = View.GONE
            number_text.visibility = View.GONE

        } else if (state == NightScoreButtonState.PAST) {
            night_button.isEnabled = true
            night_button.setPadding(0, 0, 0, 24 * density.toInt())

            dot_view.visibility = View.VISIBLE
            number_text.visibility = View.VISIBLE

            night_button.setOnClickListener {
                this.listener()
            }
        }

        night_button.text = resources.getString(R.string.night_label_btn, night)
        number_text.text = "$score"

        val redUpper = resources.getInteger(R.integer.red_upper)
        val yellowUpper = resources.getInteger(R.integer.yellow_upper)
        val dotColorID = when {
            score < redUpper -> R.drawable.red_dot
            score <= yellowUpper -> R.drawable.yellow_dot
            score > yellowUpper -> R.drawable.green_dot
            else -> R.drawable.red_dot
        }

        dot_view.background = resources.getDrawable(dotColorID, null)

        //automation
        this.view.contentDescription = "$night"

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}