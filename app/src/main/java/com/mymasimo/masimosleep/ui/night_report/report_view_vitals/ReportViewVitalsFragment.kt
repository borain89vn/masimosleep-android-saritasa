package com.mymasimo.masimosleep.ui.night_report.report_view_vitals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentReportViewVitalsBinding

class ReportViewVitalsFragment : Fragment(R.layout.fragment_report_view_vitals) {
    private val viewBinding by viewBinding(FragmentReportViewVitalsBinding::bind)

    companion object {
        fun newInstance() = ReportViewVitalsFragment()
    }

    private lateinit var onClickListener: () -> Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    fun setOnClickListener(listener: () -> Unit) {
        onClickListener = listener
    }

    private fun loadViewContent() {
        viewBinding.vitalsButton.setOnClickListener {
            onClickListener()
        }
    }
}
