package com.mymasimo.masimosleep.ui.night_report.report_view_vitals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_session_view_vitals.*


class ReportViewVitalsFragment : Fragment() {

    companion object {
        fun newInstance() = ReportViewVitalsFragment()
    }

    private lateinit var onClickListener: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_view_vitals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    fun setOnClickListener(listener: () -> Unit) {
        onClickListener = listener
    }

    private fun loadViewContent() {
        vitals_button.setOnClickListener {
            onClickListener()
        }
    }
}
