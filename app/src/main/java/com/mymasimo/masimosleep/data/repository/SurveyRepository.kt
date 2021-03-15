package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.SurveyQuestionEntityDao
import com.mymasimo.masimosleep.data.room.entity.SurveyAnswer
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestion
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestionEntity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyRepository @Inject constructor(
    private val surveyQuestionEntityDao: SurveyQuestionEntityDao,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) {
    fun saveSurvey(sessionId: Long, surveyResponse: List<Pair<SurveyQuestion, SurveyAnswer>>) {
        // Make sure there's one and only one entry for each type of question.
        SurveyQuestion.values().forEach { question ->
            if (surveyResponse.count { (surveyQuestion) -> surveyQuestion == question } != 1) {
                throw IllegalArgumentException(
                    "Survey responses should contain 1 question of each type"
                )
            }
        }

        surveyResponse.forEach { (question, answer) ->
            surveyQuestionEntityDao.insert(
                SurveyQuestionEntity(
                    question = question,
                    answer = answer,
                    sessionId = sessionId
                )
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeBy(
                    onComplete = {
                        Timber.d("Survey question: $question with answer $answer saved")
                    },
                    onError = { e ->
                        Timber.d(e, "Failed to save survey answer")
                    }
                )
                .addTo(disposables)
        }
    }

    fun getSessionSurveyEntries(sessionId: Long): Single<List<SurveyQuestionEntity>> {
        return surveyQuestionEntityDao.findAllBySessionId(sessionId)
    }
}
