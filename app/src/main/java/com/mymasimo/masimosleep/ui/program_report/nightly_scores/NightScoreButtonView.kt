package com.mymasimo.masimosleep.ui.program_report.nightly_scores

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.NightScoreButtonViewBinding

class NightScoreButtonView(context: Context, night: Int, score: Int, state: NightScoreButtonState) : ConstraintLayout(context) {

    private val state: NightScoreButtonState
    private lateinit var listener: () -> Unit
    private val viewBinding by viewBinding(NightScoreButtonViewBinding::bind)

    init {
        inflate(context, R.layout.night_score_button_view, this)

        val screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val buttonsPerRow = 5
        val availableWidth = screenWidth - 64 * density

        val params = LayoutParams(availableWidth.toInt() / buttonsPerRow, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(2 * density.toInt(), 0, 2 * density.toInt(), 0)
        this.layoutParams = params
        this.state = state

        if (state == NightScoreButtonState.FUTURE) {
            viewBinding.nightButton.isEnabled = false
            viewBinding.nightButton.setPadding(0, 0, 0, 0)

            viewBinding.dotView.visibility = View.GONE
            viewBinding.numberText.visibility = View.GONE

        } else if (state == NightScoreButtonState.PAST) {
            viewBinding.nightButton.isEnabled = true
            viewBinding.nightButton.setPadding(0, 0, 0, 24 * density.toInt())

            viewBinding.dotView.visibility = View.VISIBLE
            viewBinding.numberText.visibility = View.VISIBLE

            viewBinding.nightButton.setOnClickListener {
                this.listener()
            }
        }

        viewBinding.nightButton.text = resources.getString(R.string.night_label_btn, night)
        viewBinding.numberText.text = "$score"

        val redUpper = resources.getInteger(R.integer.red_upper)
        val yellowUpper = resources.getInteger(R.integer.yellow_upper)
        val dotColorID = when {
            score < redUpper -> R.drawable.red_dot
            score <= yellowUpper -> R.drawable.yellow_dot
            score > yellowUpper -> R.drawable.green_dot
            else -> R.drawable.red_dot
        }

        viewBinding.dotView.background = ResourcesCompat.getDrawable(resources, dotColorID, null)

        this.contentDescription = "$night"
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}