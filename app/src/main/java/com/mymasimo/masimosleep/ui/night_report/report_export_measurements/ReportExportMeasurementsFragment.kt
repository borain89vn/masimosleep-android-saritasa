package com.mymasimo.masimosleep.ui.night_report.report_export_measurements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentReportExportMeasurementsBinding

class ReportExportMeasurementsFragment : Fragment(R.layout.fragment_report_export_measurements) {
    private val viewBinding by viewBinding(FragmentReportExportMeasurementsBinding::bind)

    companion object {
        fun newInstance() = ReportExportMeasurementsFragment()
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
        viewBinding.exportButton.setOnClickListener {
            onClickListener()
        }
    }
}