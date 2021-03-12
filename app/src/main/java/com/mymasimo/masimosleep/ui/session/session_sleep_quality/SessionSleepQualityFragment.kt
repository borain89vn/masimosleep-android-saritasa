package com.mymasimo.masimosleep.ui.session.session_sleep_quality

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
import com.mymasimo.masimosleep.databinding.FragmentSessionSleepQualityBinding
import javax.inject.Inject

class SessionSleepQualityFragment : Fragment(R.layout.fragment_session_sleep_quality) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private var previousScore: Int? = null

    // Epoch timestamp of when the session started.
    private var startTimeMillis: Long = 0

    private val vm: SessionSleepQualityViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentSessionSleepQualityBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        startTimeMillis = requireArguments().getLong(KEY_SESSION_START_AT)
        vm.onCreate(startTimeMillis)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.liveScore.observe(viewLifecycleOwner) { liveScore ->
            updateScore(liveScore)
        }

        loadViewContent()
    }

    private fun loadViewContent() {
        if (previousScore == null) {
            viewBinding.trendImage.visibility = View.INVISIBLE
        }
    }

    private fun updateScore(score: Double) {
        val scoreInt = (score * 100).toInt()
        viewBinding.lblScoreText.text = scoreInt.toString()

        if (previousScore == null) {
            previousScore = scoreInt
        }

        var trend: Int = R.drawable.trend_up
        //previous score was set above, can't be null.
        if (scoreInt < previousScore!!) {
            trend = R.drawable.trend_down
        }

        if (scoreInt == previousScore!!) {
            viewBinding.trendImage.visibility = View.INVISIBLE
        } else {
            viewBinding.trendImage.visibility = View.VISIBLE
            viewBinding.trendImage.setImageDrawable(ResourcesCompat.getDrawable(resources, trend, null))
        }

        this.previousScore = scoreInt

        var triangle = R.drawable.triangle_red
        var face = R.drawable.face_red
        var qualityLevel = R.string.sq_redLabel

        viewBinding.scoreProgress.setFirstBarColor(R.color.sq_redOff)
        viewBinding.scoreProgress.setSecondBarColor(R.color.sq_yellowOff)
        viewBinding.scoreProgress.setThirdBarColor(R.color.sq_greenOff)

        when {
            scoreInt <= resources.getInteger(R.integer.red_upper) -> {
                viewBinding.scoreProgress.setFirstBarColor(R.color.sq_redOn)
            }
            scoreInt <= resources.getInteger(R.integer.yellow_upper) -> {
                viewBinding.scoreProgress.setSecondBarColor(R.color.sq_yellowOn)
                triangle = R.drawable.triangle_yellow
                face = R.drawable.face_yellow
                qualityLevel = R.string.sq_yellowLabel
            }
            scoreInt > resources.getInteger(R.integer.yellow_upper) -> {
                viewBinding.scoreProgress.setThirdBarColor(R.color.sq_greenOn)
                triangle = R.drawable.triangle_green
                face = R.drawable.face_green
                qualityLevel = R.string.sq_greenLabel
            }
        }

        viewBinding.scoreProgress.setNotchIcon(triangle)

        viewBinding.faceImage.setImageDrawable(ResourcesCompat.getDrawable(resources, face, null))
        viewBinding.qualityText.text = resources.getString(qualityLevel)

        viewBinding.scoreProgress.setScore(score.toFloat())
    }

    companion object {
        private const val KEY_SESSION_START_AT = "SESSION_START_AT"

        fun newInstance(sessionStartAt: Long) = SessionSleepQualityFragment().apply {
            arguments = bundleOf(KEY_SESSION_START_AT to sessionStartAt)
        }
    }
}
