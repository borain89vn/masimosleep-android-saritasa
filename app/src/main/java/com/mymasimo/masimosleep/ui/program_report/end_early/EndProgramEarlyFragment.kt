package com.mymasimo.masimosleep.ui.program_report.end_early

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_end_program_early.*

class EndProgramEarlyFragment : Fragment() {

    private lateinit var onClickListener: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_end_program_early, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    private fun loadViewContent() {
        end_program_button.setOnClickListener {
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