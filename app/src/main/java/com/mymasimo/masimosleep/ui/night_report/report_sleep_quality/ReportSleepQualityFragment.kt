package com.mymasimo.masimosleep.ui.night_report.report_sleep_quality

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentReportSleepQualityBinding
import com.mymasimo.masimosleep.ui.home.ShareHomeEventViewModel
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


class ReportSleepQualityFragment : Fragment(R.layout.fragment_report_sleep_quality) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: ReportSleepQualityViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentReportSleepQualityBinding::bind)
    private val homeEventVM: ShareHomeEventViewModel by activityViewModels()

    private var redUpper = 0
    private var yellowUpper = 0
    private var yellowUpperPerPixel =  0.0
    private var greenUpperPerPixel =  0.0
    private var yellowUpperPixelMax = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        vm.onCreate(requireArguments().getLong(KEY_SESSION_ID))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calculateScoreByPixel()
        vm.sessionScore.observe(viewLifecycleOwner) { score ->
            if (MasimoSleepPreferences.emulatorUsed){
                val randomScore = (60..100).random()
                updateScore(randomScore/100.0)
            } else {
                updateScore(score)
            }
        }
        viewBinding.imgInfo.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_sleepQualityDescriptionFragment)
        }
        receiveSharedEvent()
    }

    private fun receiveSharedEvent() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            homeEventVM.shareEvent.collect {  it->
                vm.onCreate(it.sessionId)
            }
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
    }

    private fun updateScore(score: Double) {

        var scoreByPixel = 0.0
        val scoreInt = (score * 100).toInt()
        viewBinding.lblScoreText.text = scoreInt.toString()

        var triangle = R.drawable.triangle_red
        var face = R.drawable.face_red
        var qualityLevel = R.string.sq_redLabel
        var qualitySubtitle = R.string.sq_redShortDesc

        viewBinding.scoreProgress.setFirstBarColor(R.color.sq_redOff_light)
        viewBinding.scoreProgress.setSecondBarColor(R.color.sq_yellowOff_light)
        viewBinding.scoreProgress.setThirdBarColor(R.color.sq_greenOff_light)

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
        private const val KEY_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: Long): ReportSleepQualityFragment {
            return ReportSleepQualityFragment().apply {
                arguments = bundleOf(KEY_SESSION_ID to sessionId)
            }
        }
    }
}