package com.mymasimo.masimosleep.ui.session.export_measurements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.repository.RawParameterReadingRepository
import com.mymasimo.masimosleep.databinding.FragmentSessionExportMeasurementsBinding
import com.mymasimo.masimosleep.service.RawParameterReadingCsvExport
import javax.inject.Inject

class SessionExportMeasurementsFragment : Fragment(R.layout.fragment_session_export_measurements)
{
    companion object {
        fun newInstance() = SessionExportMeasurementsFragment()
    }

    private lateinit var onClickListener: () -> Unit

    private val viewBinding by viewBinding(FragmentSessionExportMeasurementsBinding::bind)

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
