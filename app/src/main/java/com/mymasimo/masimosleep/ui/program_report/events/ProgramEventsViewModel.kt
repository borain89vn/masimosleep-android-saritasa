package com.mymasimo.masimosleep.ui.program_report.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.data.repository.SleepEventRepository
import com.mymasimo.masimosleep.data.room.entity.SleepEventEntity
import com.mymasimo.masimosleep.data.room.entity.SleepEventType
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ProgramEventsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    private val eventsRepository: SleepEventRepository,
    private val disposables: CompositeDisposable
) : ViewModel() {

    private val _eventsViewData = MutableLiveData<ProgramEventsViewData>()
    val eventsViewData: LiveData<ProgramEventsViewData>
        get() = _eventsViewData

    fun onCreated(programId: Long) {
        sessionRepository.getAllSessionsByProgramIdAsc(programId)
            .flatMap { sessionEntities ->
                if (sessionEntities.isEmpty()) {
                    return@flatMap Single.just(emptyList<SessionData>())
                }
                return@flatMap Single.zip(sessionEntities.map { session ->
                    eventsRepository.getAllEventsInSession(session.id ?: throw IllegalStateException()) },
                    Function<Array<Any>, List<SessionData>> { data ->
                        val sessions = mutableListOf<List<SleepEventEntity>>()
                        data.forEach { sessionData ->
                            val eventsInSession = sessionData as List<SleepEventEntity>
                            sessions.add(eventsInSession)
                        }

                        return@Function sessions.map { eventsInSession ->
                            SessionData(
                                minorEvents = eventsInSession.count { it.type == SleepEventType.MILD },
                                majorEvents = eventsInSession.count { it.type == SleepEventType.SEVERE }
                            )
                        }
                    })
            }
            .map { sessionsData -> parseProgramEvents(sessionsData) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { eventsViewData ->
                _eventsViewData.value = eventsViewData
            }
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun parseProgramEvents(
        sessionsData: List<SessionData>
    ): ProgramEventsViewData {
        val eventsByNight = mutableListOf<ProgramEventsViewData.Night>()
        for (index in 0 until NUM_OF_NIGHTS) {
            if (sessionsData.size - 1 >= index) {
                eventsByNight.add(
                    ProgramEventsViewData.Night(
                        index = index + 1,
                        minorEvents = sessionsData[index].minorEvents,
                        majorEvents = sessionsData[index].majorEvents
                    )
                )
            } else {
                eventsByNight.add(
                    ProgramEventsViewData.Night(
                        index = index + 1,
                        minorEvents = 0,
                        majorEvents = 0
                    )
                )
            }
        }


        return ProgramEventsViewData(
            totalEvents = sessionsData.sumBy { it.majorEvents } + sessionsData.sumBy { it.minorEvents },
            minorEvents = sessionsData.sumBy { it.minorEvents },
            majorEvents = sessionsData.sumBy { it.majorEvents },
            eventsByNight = eventsByNight
        )
    }

    data class SessionData(
        val minorEvents: Int,
        val majorEvents: Int
    )

    data class ProgramEventsViewData(
        val totalEvents: Int,
        val minorEvents: Int,
        val majorEvents: Int,
        val eventsByNight: List<Night>
    ) {
        data class Night(
            val index: Int,
            val minorEvents: Int,
            val majorEvents: Int
        )
    }
}
