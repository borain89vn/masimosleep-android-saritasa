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
import com.mymasimo.masimosleep.ui.night_report.report_events.ReportEventsFragment
import com.mymasimo.masimosleep.ui.night_report.report_events.util.SleepEventsViewData
import javax.inject.Inject

class ReportMeasurementsFragment : Fragment(R.layout.fragment_report_measurements) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportMeasurementsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportMeasurementsBinding::bind)

    private var sessionId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        vm.onCreate(sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        vm.measurementViewData.observe(viewLifecycleOwner){
            updateUI(it)
        }
        viewBinding.arrowIcon.setOnClickListener {
            view.findNavController().navigate(
                NightReportFragmentDirections.actionNightReportFragmentToEventDetailsFragment(
                    sessionId
                )
            )
        }
    }


    private fun updateUI(measurement: MeasurementViewData) {
        viewBinding.oxygenLevelText.text = "${measurement.oxygen_level}"
        viewBinding.pureRateText.text = "${measurement.pure_rate}"
        viewBinding.repiratoryRateText.text = "${measurement.respiratory_rate}"
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) = ReportMeasurementsFragment().apply {
            arguments = bundleOf(KEY_SESSION_ID to sessionId)
        }
    }
}