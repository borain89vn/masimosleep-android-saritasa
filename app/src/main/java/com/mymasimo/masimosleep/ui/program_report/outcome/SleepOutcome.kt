package com.mymasimo.masimosleep.ui.program_report.outcome

import com.masimo.sleepscore.sleepscorelib.SleepSessionScoreProvider

enum class SleepOutcome {
    SLIGHT,
    TRENDING_DOWN,
    STABLE,
    SIGNIFICANT;

    companion object {

        /**
         * ADR-2795A
         * Cross check with sleep-score-lib.aar
         */
        fun fromValue(outcomeValue: Double): SleepOutcome {
            val sleepScoreConstants = SleepSessionScoreProvider.CONSTANTS
            val processValue = outcomeValue * 100
            return if (processValue <= sleepScoreConstants.fMinNoImprovement) {
                TRENDING_DOWN
            } else if (processValue > 0 && processValue <= sleepScoreConstants.fMinModImprovement) {
                STABLE
            } else if (processValue > sleepScoreConstants.fMinModImprovement && processValue < sleepScoreConstants.fMinSigImprovement) {
                SLIGHT
            } else SIGNIFICANT
        }

    }
}

fun SleepOutcomeFromInt(status: Int) = SleepOutcome.values()[status]