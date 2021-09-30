package com.mymasimo.masimosleep.ui.night_report.report_measurements

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportMeasurementsBinding
import com.mymasimo.masimosleep.model.MeasurementViewData
import com.mymasimo.masimosleep.ui.night_report.NightReportFragmentDirections
import com.mymasimo.masimosleep.util.format
import javax.inject.Inject

class ReportMeasurementsFragment : Fragment(R.layout.fragment_report_measurements) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportMeasurementsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportMeasurementsBinding::bind)

    private var sessionId: Long = -1
    private var nightNumber: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        nightNumber = requireArguments().getInt(KEY_NIGHT_NUMBER)

        vm.onCreate(sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        vm.measurementViewData.observe(viewLifecycleOwner){
            updateUI(it)
        }
        viewBinding.arrowIcon.setOnClickListener {
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToReportVitalsFragment(
                    sessionId,
                    nightNumber
                )
            )
        }
        viewBinding.viewVitalTitle.setOnClickListener {
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToReportVitalsFragment(
                    sessionId,
                    nightNumber
                )
            )
        }
    }

    private fun updateUI(measurement: MeasurementViewData) {
        viewBinding.oxygenLevelText.text = "${measurement.oxygen_level.format()}"
        viewBinding.pulseRateText.text = "${measurement.pulse_rate.format()}"
        viewBinding.respiratoryRateText.text = "${measurement.respiratory_rate.format()}"
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"
        private const val KEY_NIGHT_NUMBER = "NIGHT_NUMBER"

        fun newInstance(sessionId: Long, nightNumber: Int) = ReportMeasurementsFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId, KEY_NIGHT_NUMBER to nightNumber)
        }
    }
}