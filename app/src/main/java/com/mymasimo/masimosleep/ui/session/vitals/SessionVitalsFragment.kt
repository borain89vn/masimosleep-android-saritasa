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
import java.util.*

class SessionVitalsFragment : Fragment(R.layout.fragment_session_vitals) {

    companion object {
        fun newInstance(): SessionVitalsFragment {
            return SessionVitalsFragment()
        }

        private const val SPO2_LINE_FRAGMENT_TAG = "SPO2_LINE"
        private const val PR_LINE_FRAGMENT_TAG = "PR_LINE"
        private const val RRP_LINE_FRAGMENT_TAG = "RRP_LINE"

        private val ALL_FRAGMENT_TAGS = listOf(
            SPO2_LINE_FRAGMENT_TAG,
            PR_LINE_FRAGMENT_TAG,
            RRP_LINE_FRAGMENT_TAG,
        )
    }

    val args: SessionVitalsFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentSessionVitalsBinding::bind)
    private val readingTypesToViewStyle: EnumMap<ReadingType, ViewStyle> =
        EnumMap(ReadingType::class.java)

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

        switchLinearChartsToViewStyle(ViewStyle.DAYS)

        viewBinding.allButton.setOnClickListener {
            switchLinearChartsToViewStyle(ViewStyle.DAYS)
        }

        viewBinding.hourButton.setOnClickListener {
            switchLinearChartsToViewStyle(ViewStyle.HOURS)
        }

        viewBinding.minuteButton.setOnClickListener {
            switchLinearChartsToViewStyle(ViewStyle.MINUTES)
        }
    }

    private fun updateSelection() {
        viewBinding.allButton.isSelected =
            readingTypesToViewStyle.values.all { it == ViewStyle.DAYS }
        viewBinding.hourButton.isSelected =
            readingTypesToViewStyle.values.all { it == ViewStyle.HOURS }
        viewBinding.minuteButton.isSelected =
            readingTypesToViewStyle.values.all { it == ViewStyle.MINUTES }
    }

    private fun switchLinearChartsToViewStyle(viewStyle: ViewStyle) {
        removeAllFragments()

        for (type in listOf(ReadingType.SP02, ReadingType.PR, ReadingType.RRP)) {
            readingTypesToViewStyle[type] = viewStyle
            val fragment = LiveLineGraphFragment.newInstance(type, args.sessionStart, viewStyle)
            fragment.onViewStyleChangeListener = {
                readingTypesToViewStyle[type] = it
                updateSelection()
            }
            addFragment(fragment, fragmentTagForReadingType(type))
        }

        updateSelection()
    }

    private fun fragmentTagForReadingType(readingType: ReadingType): String {
        return when (readingType) {
            ReadingType.SP02 -> SPO2_LINE_FRAGMENT_TAG
            ReadingType.PR -> PR_LINE_FRAGMENT_TAG
            ReadingType.RRP -> RRP_LINE_FRAGMENT_TAG
            ReadingType.DEFAULT -> "" // should not happen.
        }
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