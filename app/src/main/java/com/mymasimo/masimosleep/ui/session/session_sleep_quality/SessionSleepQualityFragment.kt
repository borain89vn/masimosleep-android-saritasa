package com.mymasimo.masimosleep.ui.session.session_sleep_quality

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
import kotlinx.android.synthetic.main.fragment_session_sleep_quality.*
import javax.inject.Inject

class SessionSleepQualityFragment : Fragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private var previousScore: Int? = null

    // Epoch timestamp of when the session started.
    private var startTimeMillis: Long = 0

    private val vm: SessionSleepQualityViewModel by viewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        startTimeMillis = requireArguments().getLong(KEY_SESSION_START_AT)
        vm.onCreate(startTimeMillis)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_session_sleep_quality, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.liveScore.observe(viewLifecycleOwner) { liveScore ->
            updateScore(liveScore)
        }

        loadViewContent()
    }

    private fun loadViewContent() {
        if (previousScore == null) {
            trend_image.visibility = View.INVISIBLE
        }
    }

    private fun updateScore(score: Double) {
        val scoreInt = (score * 100).toInt()
        lbl_score_text.text = scoreInt.toString()

        if (previousScore == null) {
            previousScore = scoreInt
        }

        var trend: Int = R.drawable.trend_up
        //previous score was set above, can't be null.
        if (scoreInt < previousScore!!) {
            trend = R.drawable.trend_down
        }

        if (scoreInt == previousScore!!) {
            trend_image.visibility = View.INVISIBLE
        } else {
            trend_image.visibility = View.VISIBLE
            trend_image.setImageDrawable(resources.getDrawable(trend, null))
        }

        this.previousScore = scoreInt

        var triangle = R.drawable.triangle_red
        var face = R.drawable.face_red
        var qualityLevel = R.string.sq_redLabel

        score_progress.setFirstBarColor(R.color.sq_redOff)
        score_progress.setSecondBarColor(R.color.sq_yellowOff)
        score_progress.setThirdBarColor(R.color.sq_greenOff)

        when {
            scoreInt <= resources.getInteger(R.integer.red_upper)    -> {
                score_progress.setFirstBarColor(R.color.sq_redOn)
            }
            scoreInt <= resources.getInteger(R.integer.yellow_upper) -> {
                score_progress.setSecondBarColor(R.color.sq_yellowOn)
                triangle = R.drawable.triangle_yellow
                face = R.drawable.face_yellow
                qualityLevel = R.string.sq_yellowLabel
            }
            scoreInt > resources.getInteger(R.integer.yellow_upper)  -> {
                score_progress.setThirdBarColor(R.color.sq_greenOn)
                triangle = R.drawable.triangle_green
                face = R.drawable.face_green
                qualityLevel = R.string.sq_greenLabel
            }
        }

        score_progress.setNotchIcon(triangle)

        face_image.setImageDrawable(resources.getDrawable(face, null))
        quality_text.text = resources.getString(qualityLevel)

        score_progress.setScore(score.toFloat())
    }

    companion object {
        private const val KEY_SESSION_START_AT = "SESSION_START_AT"

        fun newInstance(sessionStartAt: Long) = SessionSleepQualityFragment().apply {
            arguments = bundleOf(KEY_SESSION_START_AT to sessionStartAt)
        }
    }
}
