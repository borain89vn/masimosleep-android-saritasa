package com.mymasimo.masimosleep.ui.home.night_picker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.start_program_button_view.view.*

class StartProgramButtonView : ConstraintLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attr: AttributeSet? = null) : super(context, attr) {
        initView()
    }

    constructor(
        context: Context,
        attr: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attr, defStyleAttr) {
        initView()
    }

    private lateinit var view: View
    private lateinit var listener: () -> Unit

    private fun initView() {
        view = LayoutInflater.from(context).inflate(R.layout.start_program_button_view, this, true)

        val screenWidth = resources.displayMetrics.widthPixels
        view.layoutParams = ViewGroup.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        view.start_program_button.setOnClickListener {
            listener()
        }
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}