package com.mymasimo.masimosleep.ui.night_report.report_sleep_quality

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentReportSleepQualityBinding
import javax.inject.Inject


class ReportSleepQualityFragment : Fragment(R.layout.fragment_report_sleep_quality) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportSleepQualityViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportSleepQualityBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreate(requireArguments().getLong(KEY_SESSION_ID))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.sessionScore.observe(viewLifecycleOwner) { score ->
            updateScore(score)
        }
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

    companion object {
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long): ReportSleepQualityFragment {
            return ReportSleepQualityFragment().apply {
                arguments = bundleOf(KEY_SESSION_ID to sessionId)
            }
        }
    }
}