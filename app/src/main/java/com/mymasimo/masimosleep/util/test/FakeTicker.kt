package com.mymasimo.masimosleep.util.test

import com.masimo.common.model.universal.ParameterID
import com.masimo.sleepscore.sleepscorelib.model.Parameter
import com.mymasimo.masimosleep.base.dispatchers.CoroutineDispatchers
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.data.room.dao.ParameterReadingEntityDao
import com.mymasimo.masimosleep.data.room.dao.ProgramEntityDao
import com.mymasimo.masimosleep.data.room.dao.SessionEntityDao
import com.mymasimo.masimosleep.data.room.entity.ParameterReadingEntity
import com.mymasimo.masimosleep.data.room.entity.ReadingType
import com.mymasimo.masimosleep.data.room.entity.SessionEntity
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.model.Tick
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

class FakeTicker @Inject constructor(
    private val sleepSessionScoreManager: SleepSessionScoreManager,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val dispatchers: CoroutineDispatchers,
    private val parameterDao: ParameterReadingEntityDao,
    private val programDao: ProgramEntityDao,
    private val sessionDao: SessionEntityDao,
) {

    fun startFakeTicking() {
        // Simulate 20 empty ticks.
        for (i in 0..20) {
            emptyTick()
        }
        // Simulate 30 SPO2 only ticks.
        for (i in 0..30) {
            halfEmptyTickOnlySPO2()
        }
        // Simulate 25 SPO2 and PR only ticks.
        for (i in 0..25) {
            halfEmptyTickOnlySPO2AndPR()
        }

        var skipUntilMillis: Long? = null
        if (EMPTY_BLOCKS_ENABLED) {
            // First at the 100 second mark, then every 200 seconds..skip ticks for 150 seconds.
            Observable.interval(100, 200, TimeUnit.SECONDS)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    // Skip ticks for 150 seconds.
                    skipUntilMillis = Calendar.getInstance().timeInMillis + (150 * 1000)
                }
                .addTo(disposables)
        }

        Observable.interval(0, 10, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                val nowMillis = Calendar.getInstance().timeInMillis
                val skipUntilMillisLocal = skipUntilMillis
                val shouldTick = skipUntilMillisLocal == null || nowMillis > skipUntilMillisLocal
                if (shouldTick) {
                    sleepSessionScoreManager.sendTick(newTick())
                }
            }
            .addTo(disposables)
    }

    suspend fun createNights(count: Int) = withContext(dispatchers.io()) {
        val countTicksInNight = 8 * 60
        for (i in 1..count) {
            val calendar = Calendar.getInstance()
            val amount = -count+i-1
            calendar.add(Calendar.DAY_OF_MONTH, amount)
            val startAt = calendar.timeInMillis - countTicksInNight * 60 * 1000
            programDao.findCurrentProgram()
                .flatMapCompletable { program ->
                    sessionDao.insert(
                        SessionEntity(
                            programId = program.id ?: throw IllegalStateException(),
                            nightNumber = i,
                            startAt = startAt,
                            endAt = calendar.timeInMillis
                        )
                    )
                }.blockingGet()

            for (j in 1..countTicksInNight) {
                val createdAt = calendar.timeInMillis - j * 60 * 1000
                val (pSPO2, pPR, pRRP) = newTick()
                val parameterSPO2 = ParameterReadingEntity(
                    type = ReadingType.SP02,
                    value = pSPO2.value.toDouble(),
                    dataPointCount = 1,
                    createdAt = createdAt,
                )
                parameterDao.insert(parameterSPO2).blockingGet()

                val parameterPR = ParameterReadingEntity(
                    type = ReadingType.PR,
                    value = pPR.value.toDouble(),
                    dataPointCount = 1,
                    createdAt = createdAt,
                )
                parameterDao.insert(parameterPR).blockingGet()

                val parameterRPR = ParameterReadingEntity(
                    type = ReadingType.RRP,
                    value = pRRP.value.toDouble(),
                    dataPointCount = 1,
                    createdAt = createdAt,
                )
                parameterDao.insert(parameterRPR).blockingGet()
            }
        }
    }

    private fun emptyTick() {
        val pSPO2 = Parameter(ParameterID.FUNC_SPO2, 0f, 0x00)
        val pPR = Parameter(ParameterID.PR, 0f, 0x00)
        val pRRP = Parameter(ParameterID.RRP, 0f, 0x00)
        sleepSessionScoreManager.sendTick(Tick(pSPO2, pPR, pRRP))
    }

    private fun halfEmptyTickOnlySPO2() {
        val randomSPO2 = Random.nextDouble(94.0, 100.0)
        val pSPO2 = Parameter(ParameterID.FUNC_SPO2, randomSPO2.toFloat(), 0x00)
        val pPR = Parameter(ParameterID.PR, 0f, 0x00)
        val pRRP = Parameter(ParameterID.RRP, 0f, 0x00)
        sleepSessionScoreManager.sendTick(Tick(pSPO2, pPR, pRRP))
    }

    private fun halfEmptyTickOnlySPO2AndPR() {
        val randomSPO2 = Random.nextDouble(94.0, 100.0)
        val pSPO2 = Parameter(ParameterID.FUNC_SPO2, randomSPO2.toFloat(), 0x00)
        val randomPR = Random.nextDouble(60.0, 70.0)
        val pPR = Parameter(ParameterID.PR, randomPR.toFloat(), 0x00)
        val pRRP = Parameter(ParameterID.RRP, 0f, 0x00)
        sleepSessionScoreManager.sendTick(Tick(pSPO2, pPR, pRRP))
    }

    private fun newTick(): Tick {
        val randomSPO2 = Random.nextDouble(94.0, 100.0)
        val pSPO2 = Parameter(ParameterID.FUNC_SPO2, randomSPO2.toFloat(), 0x00)

        val randomPR = Random.nextDouble(60.0, 70.0)
        val pPR = Parameter(ParameterID.PR, randomPR.toFloat(), 0x00)

        val randomRRP = Random.nextDouble(11.0, 12.0)
        val pRRP = Parameter(ParameterID.RRP, randomRRP.toFloat(), 0x00)

        return Tick(pSPO2, pPR, pRRP)
    }

    companion object {
        private const val EMPTY_BLOCKS_ENABLED = false
    }
}
