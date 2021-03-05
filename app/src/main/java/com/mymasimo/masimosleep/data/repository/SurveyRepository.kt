package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.SessionEntityDao
import com.mymasimo.masimosleep.data.room.dao.SurveyQuestionEntityDao
import com.mymasimo.masimosleep.data.room.entity.SurveyAnswer
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestion
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestionEntity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyRepository @Inject constructor(
    private val surveyQuestionEntityDao: SurveyQuestionEntityDao,
    private val sessionEntityDao: SessionEntityDao,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) {
    fun saveSurvey(sessionId: Long, surveyResponse: List<Pair<SurveyQuestion, SurveyAnswer>>) {
        // Make sure there's one and only one entry for each type of question.
        SurveyQuestion.values().forEach { question ->
            if (surveyResponse.count { it.first == question } != 1) {
                throw IllegalArgumentException(
                    "Survey responses should contain 1 question of each type"
                )
            }
        }

        surveyResponse.forEach { response ->
            surveyQuestionEntityDao.insert(
                SurveyQuestionEntity(
                    question = response.first,
                    answer = response.second,
                    sessionId = sessionId
                )
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeBy(
                    onComplete = {
                        Timber.d("Survey question: ${response.first} with answer ${response.second} saved")
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

    fun getProgramSurveyEntries(programId: Long): Single<List<SurveyQuestionEntity>> {
        return sessionEntityDao.findAllByProgramId(programId)
            .flatMap { sessions ->
                if (sessions.isEmpty()) {
                    return@flatMap Single.just(emptyList<SurveyQuestionEntity>())
                }
                return@flatMap Single.zip(
                    sessions.map {
                        getSessionSurveyEntries(it.id ?: throw IllegalStateException())
                    },
                    Function<Array<Any>, List<SurveyQuestionEntity>> { data ->
                        val sessionsEntries = mutableListOf<List<SurveyQuestionEntity>>()
                        data.forEach { surveyEntries ->
                            sessionsEntries.add(surveyEntries as List<SurveyQuestionEntity>)
                        }
                        return@Function sessionsEntries.flatten()
                    })
            }
    }
}
