package com.mymasimo.masimosleep.ui.session.session_measurements

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportMeasurementsBinding
import com.mymasimo.masimosleep.util.format
import javax.inject.Inject

class SessionMeasurementsFragment : Fragment(R.layout.fragment_session_measurement) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: SessionMeasurementsViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportMeasurementsBinding::bind)

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        startTime = requireArguments().getLong(KEY_SESSION_START)
        vm.onCreate(startTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()

        viewBinding.arrowIcon.setOnClickListener {
            sendClickEventToParent()
        }
        viewBinding.viewVitalTitle.setOnClickListener {
            sendClickEventToParent()
        }
    }

    private fun updateUI() {
        vm.currentReadingSP02.observe(viewLifecycleOwner){ oxygen_level->
            viewBinding.oxygenLevelText.text = "${oxygen_level.format()}"
        }
        vm.currentReadingPR.observe(viewLifecycleOwner){ pulse_rate->
            viewBinding.pulseRateText.text = "${pulse_rate.format()}"
        }
        vm.currentReadingRRP.observe(viewLifecycleOwner){ respiratory_rate->
            viewBinding.respiratoryRateText.text = "${respiratory_rate.format()}"
        }
    }

    private fun sendClickEventToParent() {
        parentFragment?.setFragmentResult(KEY_REQUEST_CLICK, bundleOf(KEY_RESULT_OPEN_VITAL_DETAIL to true))
    }

    companion object {
        private const val KEY_SESSION_START = "SESSION_START"
        private const val KEY_REQUEST_CLICK = "CLICK"
        private const val KEY_RESULT_OPEN_VITAL_DETAIL = "OPEN_VITAL"
        fun newInstance(sessionStart: Long) = SessionMeasurementsFragment().apply {
            arguments = bundleOf(KEY_SESSION_START to sessionStart)
        }
    }
}