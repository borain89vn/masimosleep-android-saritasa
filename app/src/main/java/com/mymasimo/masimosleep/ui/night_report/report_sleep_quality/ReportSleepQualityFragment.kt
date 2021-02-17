package com.mymasimo.masimosleep.ui.night_report.report_sleep_quality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import kotlinx.android.synthetic.main.fragment_report_sleep_quality.*
import javax.inject.Inject


class ReportSleepQualityFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportSleepQualityViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreate(requireArguments().getLong(KEY_SESSION_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_sleep_quality, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.sessionScore.observe(viewLifecycleOwner) { score ->
            updateScore(score)
        }
    }

    private fun updateScore(score : Double) {
        val scoreInt = (score*100).toInt()
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
        face_image.setImageDrawable(resources.getDrawable(face,null))
        quality_text.text = resources.getString(qualityLevel)
        sub_title_text.text = getString(qualitySubtitle)

        score_progress.setScore(score.toFloat())
    }

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long) : ReportSleepQualityFragment {
            return ReportSleepQualityFragment().apply {
                arguments = bundleOf(KEY_SESSION_ID to sessionId)
            }
        }
    }
}