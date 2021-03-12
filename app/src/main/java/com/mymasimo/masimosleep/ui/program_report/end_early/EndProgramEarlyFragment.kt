package com.mymasimo.masimosleep.ui.program_report.end_early

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentEndProgramEarlyBinding

class EndProgramEarlyFragment : Fragment(R.layout.fragment_end_program_early) {
    private val viewBinding by viewBinding(FragmentEndProgramEarlyBinding::bind)

    private lateinit var onClickListener: () -> Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.endProgramButton.setOnClickListener {
            onClickListener()
        }
    }

    fun setOnClickListener(listener: () -> Unit) {
        onClickListener = listener
    }

    companion object {
        fun newInstance() = EndProgramEarlyFragment()
    }
}