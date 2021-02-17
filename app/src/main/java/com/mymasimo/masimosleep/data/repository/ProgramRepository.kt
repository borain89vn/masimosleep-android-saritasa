package com.mymasimo.masimosleep.data.repository

import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.ProgramEntityDao
import com.mymasimo.masimosleep.data.room.entity.ProgramEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramRepository @Inject constructor(
        private val programEntityDao: ProgramEntityDao,
        private val schedulerProvider: SchedulerProvider,
        private val disposables: CompositeDisposable
) {
    fun createProgram(): Completable {
        return programEntityDao.insert(
                ProgramEntity(startDate = Calendar.getInstance().timeInMillis)
        )
    }

    fun endProgram(programId: Long): Completable {
        return programEntityDao.findById(programId)
            .flatMapCompletable { program ->
                program.endDate = Calendar.getInstance().timeInMillis
                programEntityDao.update(program)
            }
    }

    fun getProgram(programId: Long): Single<ProgramEntity> {
        return programEntityDao.findById(programId)
    }

    fun getCurrentProgram(): Single<ProgramEntity> {
        return programEntityDao.findCurrentProgram()
    }

    fun getCurrentProgramIfExists(): Maybe<ProgramEntity> {
        return programEntityDao.findCurrentProgramIfExists()
    }

    fun getLatestProgram(): Single<ProgramEntity> {
        return programEntityDao.findLatestProgram()
    }

    fun setProgramScoreOfLatestProgram(score: Double, programId: Long) {
        programEntityDao.findById(programId)
            .flatMapCompletable { program ->
                program.score = score
                programEntityDao.update(program)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                    onComplete = {
                        Timber.d("Program score saved")
                    },
                    onError = {
                        Timber.d("Error saving program score")
                    }
            )
            .addTo(disposables)
    }

    fun getAllPrograms(): Single<List<ProgramEntity>> {
        return programEntityDao.findAllDesc()
    }

    fun setProgramOutcomeOfProgram(outcomeValue: Double, id: Long) {
        programEntityDao.findById(id)
            .flatMapCompletable { program ->
                program.outcome = outcomeValue
                programEntityDao.update(program)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                    onComplete = {
                        Timber.d("Program outcome saved + $outcomeValue")
                    },
                    onError = {
                        Timber.d("Error saving program outcome")
                    }
            )
            .addTo(disposables)
    }

    fun getProgramFlowable(programId: Long): Flowable<ProgramEntity> {
        return programEntityDao.findByIdFlowable(programId)
    }
}
