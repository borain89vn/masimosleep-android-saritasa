package com.mymasimo.masimosleep.ui.session.vitals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.ViewStyle
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.databinding.FragmentSessionVitalsBinding
import com.mymasimo.masimosleep.ui.session.vitals.live.linegraph.LiveLineGraphFragment

class SessionVitalsFragment : Fragment(R.layout.fragment_session_vitals) {

    companion object {
        fun newInstance(): SessionVitalsFragment {
            return SessionVitalsFragment()
        }

        private const val SPO2_LINE_FRAGMENT_TAG = "SPO2_LINE"
        private const val SPO2_INTERVAL_FRAGMENT_TAG = "SPO2_INTERVAL"
        private const val PR_LINE_FRAGMENT_TAG = "PR_LINE"
        private const val PR_INTERVAL_FRAGMENT_TAG = "PR_INTERVAL"
        private const val RRP_LINE_FRAGMENT_TAG = "RRP_LINE"
        private const val RRP_INTERVAL_FRAGMENT_TAG = "RRP_INTERVAL"

        private val ALL_FRAGMENT_TAGS = listOf(
            SPO2_LINE_FRAGMENT_TAG,
            SPO2_INTERVAL_FRAGMENT_TAG,
            PR_LINE_FRAGMENT_TAG,
            PR_INTERVAL_FRAGMENT_TAG,
            RRP_LINE_FRAGMENT_TAG,
            RRP_INTERVAL_FRAGMENT_TAG
        )
    }

    val args: SessionVitalsFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentSessionVitalsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.sessionStart
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.backButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        viewBinding.allButton.setOnClickListener {
            clearSelection()
            viewBinding.allButton.isSelected = true
            showLinearCharts(ViewStyle.DAYS)

        }

        viewBinding.hourButton.setOnClickListener {
            clearSelection()
            viewBinding.hourButton.isSelected = true
            showLinearCharts(ViewStyle.HOURS)
        }

        viewBinding.minuteButton.setOnClickListener {
            clearSelection()
            viewBinding.minuteButton.isSelected = true
            showLinearCharts(ViewStyle.MINUTES)
        }

    }

    private fun clearSelection() {
        viewBinding.allButton.isSelected = false
        viewBinding.hourButton.isSelected = false
        viewBinding.minuteButton.isSelected = false
    }

    private fun showLinearCharts(viewStyle: ViewStyle) {
        removeAllFragments()

        addFragment(
            LiveLineGraphFragment.newInstance(ReadingType.SP02, args.sessionStart, viewStyle),
            SPO2_LINE_FRAGMENT_TAG
        )
        addFragment(
            LiveLineGraphFragment.newInstance(ReadingType.PR, args.sessionStart, viewStyle),
            PR_LINE_FRAGMENT_TAG
        )
        addFragment(
            LiveLineGraphFragment.newInstance(ReadingType.RRP, args.sessionStart, viewStyle),
            RRP_LINE_FRAGMENT_TAG
        )

    }

    private fun removeAllFragments() {
        ALL_FRAGMENT_TAGS.forEach { tag ->
            parentFragmentManager.findFragmentByTag(tag)?.let { fragment ->
                parentFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            }
        }

    }

    private fun addFragment(fragment: Fragment, tag: String) {
        parentFragmentManager.beginTransaction()
            .add(R.id.vitals_layout, fragment, tag)
            .commitAllowingStateLoss()
    }
}