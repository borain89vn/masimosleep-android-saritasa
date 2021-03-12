package com.mymasimo.masimosleep.ui.home.night_picker

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.StartProgramButtonViewBinding

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

    private lateinit var listener: () -> Unit
    private val viewBinding by viewBinding(StartProgramButtonViewBinding::bind)

    private fun initView() {
        inflate(context, R.layout.start_program_button_view, this)

        val screenWidth = resources.displayMetrics.widthPixels
        this.layoutParams = ViewGroup.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        viewBinding.startProgramButton.setOnClickListener {
            listener()
        }
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}