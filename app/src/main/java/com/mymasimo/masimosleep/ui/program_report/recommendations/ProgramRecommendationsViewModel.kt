package com.mymasimo.masimosleep.ui.program_report.recommendations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SurveyRepository
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import com.mymasimo.masimosleep.data.room.entity.SurveyAnswer
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestion
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestionEntity
import com.mymasimo.masimosleep.ui.night_report.recommendations.MIN_SLEEP_RECOMMEND_HOURS
import com.mymasimo.masimosleep.ui.night_report.recommendations.util.Recommendation
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ProgramRecommendationsViewModel @Inject constructor(
    private val surveyRepository: SurveyRepository,
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _recommendations = MutableLiveData<Set<Recommendation>>()
    val recommendations: LiveData<Set<Recommendation>>
        get() = _recommendations

    fun onCreated(programId: Long) {
        sessionRepository.getAllSessionsByProgramId(programId)
            .flatMap { sessions ->
                if (sessions.isEmpty()) {
                    return@flatMap Single.just(emptySet<Recommendation>())
                }
                return@flatMap Single.zip(
                    sessions.map { sessionEntity ->
                        surveyRepository.getSessionSurveyEntries(sessionEntity.id ?: throw IllegalStateException())
                            .zipWith(sessionRepository.getSessionById(sessionEntity.id ?: throw IllegalStateException()),
                                { entries, session -> parseRecommendationsFromSurvey(entries, session) })
                    },
                    Function<Array<Any>, Set<Recommendation>> { data ->
                        val recommendations = mutableSetOf<Recommendation>()
                        data.forEach {
                            recommendations.addAll(it as Set<Recommendation>)
                        }
                        return@Function recommendations
                    })
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { recommendations ->
                _recommendations.value = recommendations
            }.addTo(disposables)

    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun parseRecommendationsFromSurvey(
        entries: List<SurveyQuestionEntity>,
        sessionEntity: SessionEntity
    ): Set<Recommendation> {
        val recommendations = mutableSetOf<Recommendation>()
        entries.forEach { entry ->
            getRecommendationForAnswer(entry)?.let { recommendation ->
                recommendations.add(recommendation)
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