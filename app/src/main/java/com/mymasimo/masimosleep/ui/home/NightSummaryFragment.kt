package com.mymasimo.masimosleep.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import kotlinx.android.synthetic.main.fragment_night_summary.*
import kotlinx.android.synthetic.main.fragment_night_summary.face_image
import kotlinx.android.synthetic.main.fragment_night_summary.lbl_score_text
import kotlinx.android.synthetic.main.fragment_night_summary.quality_text
import kotlinx.android.synthetic.main.fragment_night_summary.score_progress
import kotlinx.android.synthetic.main.fragment_night_summary.sub_title_text
import javax.inject.Inject

class NightSummaryFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: NightSummaryViewModel by viewModels { vmFactory }

    private var sessionId: Long = -1
    private var nightNumber: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        sessionId = requireArguments().getLong(KEY_SESSION_ID)
        nightNumber = requireArguments().getInt(KEY_NIGHT_NUMBER)
        vm.onCreated(sessionId)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_night_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.summaryViewData.observe(viewLifecycleOwner) { viewData ->
            loadViewContent(viewData)
        }

        view_summary_button.setOnClickListener {
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
        lbl_score_text.text = scoreInt.toString()

        var triangle = R.drawable.triangle_red
        var face = R.drawable.face_red
        var qualityLevel = R.string.sq_redLabel
        var qualitySubtitle = R.string.sq_redSubtitle

        score_progress.setFirstBarColor(R.color.sq_redOff_light)
        score_progress.setSecondBarColor(R.color.sq_yellowOff_light)
        score_progress.setThirdBarColor(R.color.sq_greenOff_light)

        when {
            scoreInt <= resources.getInteger(R.integer.red_upper)    -> {
                score_progress.setFirstBarColor(R.color.sq_redOn)
            }
            scoreInt <= resources.getInteger(R.integer.yellow_upper) -> {
                score_progress.setSecondBarColor(R.color.sq_yellowOn)
                triangle = R.drawable.triangle_yellow
                face = R.drawable.face_yellow
                qualityLevel = R.string.sq_yellowLabel
                qualitySubtitle = R.string.sq_yellowSubtitle
            }
            scoreInt > resources.getInteger(R.integer.yellow_upper)  -> {
                score_progress.setThirdBarColor(R.color.sq_greenOn)
                triangle = R.drawable.triangle_green
                face = R.drawable.face_green
                qualityLevel = R.string.sq_greenLabel
                qualitySubtitle = R.string.sq_greenSubtitle
            }
        }

        score_progress.setNotchIcon(triangle)
        face_image.setImageDrawable(resources.getDrawable(face, null))
        quality_text.text = resources.getString(qualityLevel)
        sub_title_text.text = getString(qualitySubtitle)

        score_progress.setScore(score.toFloat())
    }

    private fun updateEventCount(totalEvents: Int) {
        event_text.text = totalEvents.toString()
        total_events_title.text = resources.getQuantityString(R.plurals.events, totalEvents)
    }

    private fun updateSleepTime(durationMinutes: Int) {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        val elapsedString = if (hours == 0) {
            "${minutes}m"
        } else {
            "${hours}h ${minutes}m"
        }

        sleep_text.text = elapsedString
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