package com.mymasimo.masimosleep.ui.night_report.recommendations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mymasimo.masimosleep.MasimoSleepApp
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.repository.SurveyRepository
import com.mymasimo.masimosleep.data.room.entity.*
import com.mymasimo.masimosleep.ui.night_report.recommendations.util.Recommendation
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val MIN_SLEEP_RECOMMEND_HOURS = 7

class RecommendationsViewModel @Inject constructor(
    app: Application,
    private val surveyRepository: SurveyRepository,
    private val sessionRepository: SessionRepository,
    private val scoreRepository: SleepScoreRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposable: CompositeDisposable
) : AndroidViewModel(app) {

    private val _recommendations = MutableLiveData<Set<Recommendation>>()
    val recommendations: LiveData<Set<Recommendation>>
        get() = _recommendations

    fun onCreated(sessionId: Long) {
        Observables.zip(
            surveyRepository.getSessionSurveyEntries(sessionId).toObservable(),
            sessionRepository.getSessionById(sessionId).toObservable(),
            scoreRepository.getSessionScore(sessionId).toObservable()
        ) { entries, session, score ->
            parseRecommendationsFromSurvey(entries, session, score)
        }.subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { recommendations ->
                _recommendations.value = recommendations
            }
            .addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    private fun parseRecommendationsFromSurvey(
        entries: List<SurveyQuestionEntity>,
        sessionEntity: SessionEntity,
        scoreEntity: ScoreEntity
    ): Set<Recommendation> {
        val recommendations = mutableSetOf<Recommendation>()
        val scoreInt = (scoreEntity.value * 100).toInt()
        if (scoreInt <= getApplication<MasimoSleepApp>().resources.getInteger(R.integer.yellow_upper)) { //Not GOOD
            entries.forEach { entry ->
                getRecommendationForAnswer(entry)?.let { recommendation ->
                    recommendations.add(recommendation)
                }
            }
        }

        sessionEntity.endAt?.let { endAt ->
            if (TimeUnit.MILLISECONDS.toHours(endAt - sessionEntity.startAt) < MIN_SLEEP_RECOMMEND_HOURS) {
                recommendations.add(Recommendation.SLEEP_HOURS)
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add(Recommendation.MAINTAIN_HEALTHY_LIFESTYLE)
        }

        return recommendations
    }

    private fun getRecommendationForAnswer(entry: SurveyQuestionEntity): Recommendation? {
        if (entry.answer == SurveyAnswer.NO_ANSWER) return null

        return when (entry.question) {
            SurveyQuestion.CAFFEINE_ -> {
                if (entry.answer == SurveyAnswer.YES) Recommendation.NO_CAFFEINE else null
            }
            SurveyQuestion.SNORING -> {
                if (entry.answer == SurveyAnswer.YES) Recommendation.SLEEP_SIDEWAYS else null
            }
            SurveyQuestion.ALCOHOL -> {
                if (entry.answer == SurveyAnswer.YES) Recommendation.NO_ALCOHOL else null
            }
            SurveyQuestion.EXERCISE -> {
                if (entry.answer == SurveyAnswer.NO) Recommendation.EXERCISE else null
            }
            SurveyQuestion.SLEEP_DRUG -> {
                if (entry.answer == SurveyAnswer.YES) Recommendation.SLEEP_AID else null
            }
        }
    }
}