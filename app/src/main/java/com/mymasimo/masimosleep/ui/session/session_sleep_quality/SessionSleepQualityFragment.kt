package com.mymasimo.masimosleep.ui.session.session_sleep_quality

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
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
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

    private var redUpper = 0
    private var yellowUpper = 0
    private var yellowUpperPerPixel =  0.0
    private var greenUpperPerPixel =  0.0
    private var yellowUpperPixelMax = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        startTimeMillis = requireArguments().getLong(KEY_SESSION_START_AT)
        vm.onCreate(startTimeMillis)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calculateScoreByPixel()
        vm.liveScore.observe(viewLifecycleOwner) { liveScore ->
            updateScore(liveScore)
        }
        viewBinding.imgInfo.setOnClickListener {
            view.findNavController().navigate(R.id.action_sessionFragment_to_sleepQualityDescriptionFragment)
        }

        loadViewContent()
    }

    private fun loadViewContent() {
        if (previousScore == null) {
            viewBinding.trendImage.visibility = View.INVISIBLE
        }
    }


    /**
     * Calculate proportion  of progress bar by design
     * We just calculate proportion of progress bar from yellow to green zone
     *
     * 0..........................60...........88............100
     *
     * Yellow zone position is from 60 to 88, total pixel is 69 pixels
     * Green zone position is from 88 to 100, total pixel is 81 pixels
     * Total pixel of green and yellow zone is 150
     * Total point of green and yellow zone is (100 -60) = 40 points
     * Total point per pixel in yellow zone is  (69/150)*40
     * Total point per pixel in green zone is (81/150)*40
     * 1 point per pixel in yellow zone is  18.4/(88-60)
     * 1 point per pixel in green zone  is 12.6/(100-88)
     */
    private fun calculateScoreByPixel(){
        redUpper = resources.getInteger(R.integer.red_upper)
        yellowUpper = resources.getInteger(R.integer.yellow_upper)

        yellowUpperPerPixel = ((69.0/150.0)*40)/(yellowUpper-redUpper)
        yellowUpperPixelMax = redUpper + (yellowUpper-redUpper)*yellowUpperPerPixel

        greenUpperPerPixel = ((81/150.0)*40)/(100-yellowUpper)

        if (MasimoSleepPreferences.emulatorUsed){
            val randomScore = (0..100).random()
            updateScore(randomScore/100.0)
        }
    }

    private fun updateScore(score: Double) {
        var scoreByPixel = 0.0
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
        var qualitySubtitle = R.string.sq_redShortDesc

        viewBinding.scoreProgress.setFirstBarColor(R.color.sq_redOff)
        viewBinding.scoreProgress.setSecondBarColor(R.color.sq_yellowOff)
        viewBinding.scoreProgress.setThirdBarColor(R.color.sq_greenOff)

        when {
            scoreInt <= resources.getInteger(R.integer.red_upper) -> {
                scoreByPixel = score
                viewBinding.scoreProgress.setFirstBarColor(R.color.sq_redOn)
            }
            scoreInt <= resources.getInteger(R.integer.yellow_upper) -> {
                scoreByPixel = (redUpper + (scoreInt - redUpper)*yellowUpperPerPixel)/100
                viewBinding.scoreProgress.setSecondBarColor(R.color.sq_yellowOn)
                triangle = R.drawable.triangle_yellow
                face = R.drawable.face_yellow
                qualityLevel = R.string.sq_yellowLabel
                qualitySubtitle = R.string.sq_yellowShortDesc
            }
            scoreInt > resources.getInteger(R.integer.yellow_upper) -> {
                scoreByPixel = (yellowUpperPixelMax + (scoreInt-yellowUpper)*greenUpperPerPixel)/100
                viewBinding.scoreProgress.setThirdBarColor(R.color.sq_greenOn)
                triangle = R.drawable.triangle_green
                face = R.drawable.face_green
                qualityLevel = R.string.sq_greenLabel
                qualitySubtitle = R.string.sq_greenShortDesc
            }
        }

        viewBinding.scoreProgress.setNotchIcon(triangle)

        viewBinding.faceImage.setImageDrawable(ResourcesCompat.getDrawable(resources, face, null))
        viewBinding.qualityText.text = resources.getString(qualityLevel)
        viewBinding.subTitleText.text = getString(qualitySubtitle)
        viewBinding.scoreProgress.setScore(scoreByPixel.toFloat())
    }

    companion object {
        private const val KEY_SESSION_START_AT = "SESSION_START_AT"

        fun newInstance(sessionStartAt: Long) = SessionSleepQualityFragment().apply {
            arguments = bundleOf(KEY_SESSION_START_AT to sessionStartAt)
        }
    }
}
