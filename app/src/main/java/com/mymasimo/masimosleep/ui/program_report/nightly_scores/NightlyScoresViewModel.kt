package com.mymasimo.masimosleep.ui.program_report.nightly_scores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.room.entity.ScoreEntity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class NightlyScoresViewModel @Inject constructor(
    private val scoreRepository: SleepScoreRepository,
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _scores = MutableLiveData<List<NightlyScoreItem>>()
    val scores: LiveData<List<NightlyScoreItem>>
        get() = _scores

    fun onCreate(programId: Long) {
        sessionRepository.getAllSessionsByProgramIdAsc(programId)
            .flatMap { sessions ->
                if (sessions.isEmpty()) {
                    return@flatMap Single.just(emptyList<NightlyScoreItem>())
                }
                return@flatMap Single.zip(sessions.map { session ->
                    scoreRepository.getSessionScore(session.id ?: throw IllegalStateException()) },
                    Function<Array<Any>, List<NightlyScoreItem>> { data ->
                        val nightItems = mutableListOf<NightlyScoreItem>()
                        data.forEachIndexed { index, dataItem ->
                            val scoreEntity = dataItem as ScoreEntity
                            val nightlyScoreItem = NightlyScoreItem(
                                sessionId = sessions[index].id!!,
                                nightNumber = index + 1,
                                score = scoreEntity.value
                            )
                            nightItems.add(nightlyScoreItem)
                        }
                        return@Function nightItems
                    })
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { scores ->
                _scores.value = scores.map { it }
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

data class NightlyScoreItem (
    val sessionId : Long,
    val nightNumber: Int,
    val score : Double
)
