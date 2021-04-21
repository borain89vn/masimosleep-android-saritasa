package com.mymasimo.masimosleep.ui.program_report.avg_sleep_quality

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.timelinechart.data.InputData
import com.masimo.timelinechart.formatter.AxisFormatter
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentAverageSleepQualityBinding
import com.mymasimo.masimosleep.ui.program_report.outcome.SleepOutcome
import javax.inject.Inject

class AverageSleepQualityFragment : Fragment(R.layout.fragment_average_sleep_quality) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: AverageSleepQualityViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentAverageSleepQualityBinding::bind)

    private var programId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        programId = requireArguments().getLong(KEY_PROGRAM_ID)
        vm.onCreated(programId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()

        vm.score.observe(viewLifecycleOwner) { scoreWithSessions ->
            updateScore(scoreWithSessions)
        }

        vm.trendData.observe(viewLifecycleOwner) { viewDate ->
            updateChart(viewDate)
        }

        vm.sleepQualityDesc.observe(viewLifecycleOwner) { scoreWithOutcome ->
            updateSleepQualityDesc(scoreWithOutcome)
        }
    }

    private fun loadViewContent() {
        viewBinding.infoButton.setOnClickListener {
            requireView().findNavController().navigate(
                R.id.action_programReportFragment_to_sleepQualityDescriptionFragment
            )
        }

        viewBinding.chartSleepScore.setAxisXPageStep(10, 5)
        viewBinding.chartSleepScore.setAxisXFormatter(object : AxisFormatter {
            override fun formatData(value: Float): String = value.toInt().toString()
        })
        viewBinding.chartSleepScore.setShowCirclePoint(true)
    }

    private fun updateSleepQualityDesc(scoreWithOutcome: Triple<Double, SleepOutcome, Int>) {
        val (score, outcome, sessionCount) = scoreWithOutcome
        if (sessionCount >= NUM_OF_NIGHTS - 1) {
            val scoreInt = (score * 100).toInt()

            var qualityDesc = R.string.program_quality_desc_poor
            when {
                scoreInt <= resources.getInteger(R.integer.red_upper) -> {
                    //POOR
                    qualityDesc = R.string.program_quality_desc_poor
                }
                scoreInt <= resources.getInteger(R.integer.yellow_upper) && scoreInt > resources.getInteger(R.integer.red_upper) -> {
                    //FAIR
                    qualityDesc = when (outcome) {
                        SleepOutcome.SLIGHT, SleepOutcome.SIGNIFICANT -> {
                            R.string.program_quality_desc_fair_trend_up
                        }

                        SleepOutcome.TRENDING_DOWN, SleepOutcome.STABLE -> {
                            R.string.program_quality_desc_fair_trend_down
                        }

                    }
                }
                scoreInt > resources.getInteger(R.integer.yellow_upper) -> {
                    qualityDesc = R.string.program_quality_desc_good
                }
            }
            viewBinding.qualityDesc.text = resources.getString(qualityDesc)
        }
    }

    private fun updateScore(scoreWithSessions: Pair<Double, Int>) {
        val (score, sessionCount) = scoreWithSessions
        if (sessionCount >= NUM_OF_NIGHTS - 1) {
            viewBinding.qualitySoFarText.text = getString(R.string.average_sleep_quality_index)

            val scoreInt = (score * 100).toInt()
            viewBinding.lblScoreText.text = scoreInt.toString()

            var face = R.drawable.face_red
            var qualityLevel = R.string.sq_redLabel

            when {
                scoreInt <= resources.getInteger(R.integer.red_upper) -> {
                }
                scoreInt <= resources.getInteger(R.integer.yellow_upper) && scoreInt > resources.getInteger(R.integer.red_upper) -> {
                    face = R.drawable.face_yellow
                    qualityLevel = R.string.sq_yellowLabel
                }
                scoreInt > resources.getInteger(R.integer.yellow_upper) -> {
                    face = R.drawable.face_green
                    qualityLevel = R.string.sq_greenLabel
                }
            }

            viewBinding.faceImage.setImageDrawable(ResourcesCompat.getDrawable(resources, face, null))
            viewBinding.qualityText.text = resources.getString(qualityLevel)
        } else {
            viewBinding.qualitySoFarText.text = getString(R.string.program_report_not_enough_nights)
        }
    }

    private fun updateChart(trendData: AverageSleepQualityViewModel.ProgramSleepQualityTrendViewData) {
        val chartData: ArrayList<InputData> = ArrayList()
        val colorList: ArrayList<Int> = ArrayList()

        for (point in trendData.sessions) {
            if (point.score.isNaN()) {
                continue
            }

            val score = (point.score * 100.0).toFloat()
            val night = (point.index + 1).toFloat()
            chartData.add(InputData(night, score))

            var circleColorId = R.color.subtleGray
            if (score.toInt() <= resources.getInteger(R.integer.red_upper)) {
                circleColorId = R.color.sq_redOn
            } else if (score.toInt() <= resources.getInteger(R.integer.yellow_upper)) {
                circleColorId = R.color.sq_yellowOn
            } else if (score.toInt() > resources.getInteger(R.integer.yellow_upper)) {
                circleColorId = R.color.sq_greenOn
            }

            colorList.add(resources.getColor(circleColorId, null))
        }

        viewBinding.chartSleepScore.setData(chartData, ArrayList())
        viewBinding.chartSleepScore.invalidate()
    }

    companion object {
        private const val KEY_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Long) = AverageSleepQualityFragment().apply {
            arguments = bundleOf(KEY_PROGRAM_ID to programId)
        }
    }
}