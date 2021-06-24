package com.mymasimo.masimosleep.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentNightSummaryBinding
import javax.inject.Inject

class NightSummaryFragment : Fragment(R.layout.fragment_night_summary) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: NightSummaryViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentNightSummaryBinding::bind)

    private var sessionId: Long = -1
    private var nightNumber: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        nightNumber = requireArguments().getInt(KEY_NIGHT_NUMBER)
        vm.onCreated(sessionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.summaryViewData.observe(viewLifecycleOwner) { viewData ->
            loadViewContent(viewData)
        }

        viewBinding.viewSummaryButton.setOnClickListener {
            view.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToNightReportFragment(
                    sessionId = sessionId,
                    nightNumber = nightNumber
                )
            )
        }
    }

    private fun loadViewContent(viewData: NightSummaryViewModel.SummaryViewData) {
        updateScore(viewData.sleepScore)
        updateEventCount(viewData.numEvents)
        updateSleepTime(viewData.timeSleptMinutes)

    }

    private fun updateScore(score: Double) {
        val scoreInt = (score * 100).toInt()
        viewBinding.lblScoreText.text = scoreInt.toString()

        var triangle = R.drawable.triangle_red
        var face = R.drawable.face_red
        var qualityLevel = R.string.sq_redLabel
        var qualitySubtitle = R.string.sq_redSubtitle

        viewBinding.scoreProgress.setFirstBarColor(R.color.sq_redOff_light)
        viewBinding.scoreProgress.setSecondBarColor(R.color.sq_yellowOff_light)
        viewBinding.scoreProgress.setThirdBarColor(R.color.sq_greenOff_light)

        when {
            scoreInt <= resources.getInteger(R.integer.red_upper) -> {
                viewBinding.scoreProgress.setFirstBarColor(R.color.sq_redOn)
            }
            scoreInt <= resources.getInteger(R.integer.yellow_upper) -> {
                viewBinding.scoreProgress.setSecondBarColor(R.color.sq_yellowOn)
                triangle = R.drawable.triangle_yellow
                face = R.drawable.face_yellow
                qualityLevel = R.string.sq_yellowLabel
                qualitySubtitle = R.string.sq_yellowSubtitle
            }
            scoreInt > resources.getInteger(R.integer.yellow_upper) -> {
                viewBinding.scoreProgress.setThirdBarColor(R.color.sq_greenOn)
                triangle = R.drawable.triangle_green
                face = R.drawable.face_green
                qualityLevel = R.string.sq_greenLabel
                qualitySubtitle = R.string.sq_greenSubtitle
            }
        }

        viewBinding.scoreProgress.setNotchIcon(triangle)
        viewBinding.faceImage.setImageDrawable(ResourcesCompat.getDrawable(resources, face, null))
        viewBinding.qualityText.text = resources.getString(qualityLevel)
        viewBinding.subTitleText.text = getString(qualitySubtitle)

        viewBinding.scoreProgress.setScore(score.toFloat())
    }

    private fun updateEventCount(totalEvents: Int) {
        viewBinding.eventText.text = totalEvents.toString()
        viewBinding.totalEventsTitle.text = resources.getQuantityString(R.plurals.events, totalEvents)
    }

    private fun updateSleepTime(durationMinutes: Int) {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        val elapsedString = if (hours == 0) {
            resources.getString(R.string.time_minutes, minutes)
        } else {
            resources.getString(R.string.time_hours_minutes, hours, minutes)
        }

        viewBinding.sleepText.text = elapsedString
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"
        private const val KEY_NIGHT_NUMBER = "NIGHT_NUMBER"

        fun newInstance(sessionId: Long, nightNumber: Int): NightSummaryFragment {
            return NightSummaryFragment().apply {
                arguments = bundleOf(
                    KEY_SESSION_ID to sessionId,
                    KEY_NIGHT_NUMBER to nightNumber
                )
            }
        }
    }
}