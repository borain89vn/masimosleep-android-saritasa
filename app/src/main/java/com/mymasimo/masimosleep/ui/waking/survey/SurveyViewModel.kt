package com.mymasimo.masimosleep.ui.waking.survey

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SurveyRepository
import com.mymasimo.masimosleep.data.room.entity.SurveyAnswer
import com.mymasimo.masimosleep.data.room.entity.SurveyQuestion
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class SurveyViewModel @Inject constructor(
        private val surveyRepository: SurveyRepository,
        private val programRepository: ProgramRepository,
        private val sessionRepository: SessionRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private var sessionId: Long? = null

    private val questionAnswerMap = mutableMapOf<SurveyQuestion, SurveyAnswer>()

    private val _enableButton = MutableLiveData<ButtonAction>()
    val enableButton: LiveData<ButtonAction>
        get() = _enableButton

    fun onCreated(sessionId: Long) {
        this.sessionId = sessionId

        programRepository.getLatestProgram()
            .flatMap { program ->
                sessionRepository.getAllSessionsByProgramId(
                        program.id ?: throw IllegalStateException())
                    .map { sessionsInProgram -> sessionsInProgram.size >= NUM_OF_NIGHTS }
            }
            .doOnError {
                Timber.d("All programs are ended")
            }
            .onErrorReturnItem(true)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ programEnded ->
                           _enableButton.value = ButtonAction(programEnded = programEnded)
                       },
                       {
                           it.printStackTrace()
                       })
            .addTo(disposables)


        SurveyQuestion.values().forEach { question ->
            questionAnswerMap[question] = SurveyAnswer.NO_ANSWER
        }
    }

    fun onQuestionAnswered(question: SurveyQuestion, answer: SurveyAnswer) {
        questionAnswerMap[question] = answer
    }

    fun onSubmitClick() {
        saveAnswers()
    }

    fun onSkipClicked() {
        saveAnswers()
    }

    private fun saveAnswers() {
        val sessionId = this.sessionId
            ?: throw IllegalStateException("Null session id. Did you forget to call onCreated()?")

        val questionAnswerPairs = questionAnswerMap.map { entry -> entry.key to entry.value }
        surveyRepository.saveSurvey(sessionId, questionAnswerPairs)
    }

    data class ButtonAction(
            val programEnded: Boolean
    )
}
