package com.mymasimo.masimosleep.ui.program_report.avg_sleep_quality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepScoreRepository
import com.mymasimo.masimosleep.data.room.entity.ProgramEntity
import com.mymasimo.masimosleep.data.room.entity.ScoreEntity
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class AverageSleepQualityViewModel @Inject constructor(
        private val programRepository: ProgramRepository,
        private val sessionRepository: SessionRepository,
        private val scoreRepository: SleepScoreRepository,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) : ViewModel() {

    private val _score = MutableLiveData<Pair<Double, Int>>()
    val score: LiveData<Pair<Double, Int>>
        get() = _score

    private val _trendData = MutableLiveData<ProgramSleepQualityTrendViewData>()
    val trendData: LiveData<ProgramSleepQualityTrendViewData>
        get() = _trendData

    private val _sleepQualityDesc = MutableLiveData<Triple<Double, Double, Int>>()
    val sleepQualityDesc: LiveData<Triple<Double, Double, Int>>
        get() = _sleepQualityDesc

    fun onCreated(programId: Long) {
        programRepository.getProgram(programId)
            .flatMap { program ->
                sessionRepository.getAllSessionsByProgramIdAsc(programId)
                    .observeOn(schedulerProvider.ui())
                    .doOnEvent { sessions, e ->
                        if (e == null) {
                            _score.value = program.score to sessions.size
                        }
                    }.observeOn(schedulerProvider.io())
                    .flatMap { sessions ->
                        if (sessions.isEmpty()) {
                            Single.just(emptyList())
                        } else Single.zip(sessions.map { session ->
                            scoreRepository.getSessionScore(session.id ?: throw IllegalStateException())
                        },
                                          Function<Array<Any>, List<Double>> { data ->
                                              val sessionScores = mutableListOf<ScoreEntity>()
                                              data.forEach {
                                                  sessionScores.add(it as ScoreEntity)
                                              }
                                              return@Function sessionScores.map { it.value }
                                          })
                    }
            }.subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { scores ->
                _trendData.value = ProgramSleepQualityTrendViewData(
                        sessions = scores.mapIndexed { index, score ->
                            ProgramSleepQualityTrendViewData.Session(
                                    index, score
                            )
                        }
                )
            }
            .addTo(disposables)

        programRepository.getProgramFlowable(programId)
            .zipWith(sessionRepository.getAllSessionsByProgramIdAsc(programId).toFlowable(), BiFunction<ProgramEntity, List<SessionEntity>, Triple<Double, Double, Int>> { program, sessions ->
                Triple(program.score, program.outcome, sessions.size)
            })
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { result ->
                _sleepQualityDesc.postValue(result)

            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    data class ProgramSleepQualityTrendViewData(
            val sessions: List<Session>
    ) {
        data class Session(
                val index: Int,
                val score: Double
        )
    }
}
