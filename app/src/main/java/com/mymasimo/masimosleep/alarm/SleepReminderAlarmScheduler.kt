package com.mymasimo.masimosleep.alarm

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import com.mymasimo.masimosleep.MainActivity
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.alarm.NotificationPublisher.Companion.KEY_NOTIFICATION
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.data.repository.ProgramRepository
import com.mymasimo.masimosleep.data.repository.SessionRepository
import com.mymasimo.masimosleep.util.CHANNEL_REMINDERS
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SleepReminderAlarmScheduler @Inject constructor(
    private val context: Context,
    private val programRepository: ProgramRepository,
    private val sessionRepository: SessionRepository,
    private val schedulerProvider: SchedulerProvider,
    private val disposables: CompositeDisposable
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarmsAsync() {
        disposables.clear()
        programRepository.getCurrentProgramIfExists()
            .flatMap { program ->
                sessionRepository.countAllSessionsInProgram(
                    program.id ?: throw IllegalStateException()
                ).toMaybe()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribeBy(
                onSuccess = { sessionsInProgram ->
                    scheduleAlarms(numNightsRemaining = NUM_OF_NIGHTS - sessionsInProgram)
                },
                onError = {},
                onComplete = {}
            )
            .addTo(disposables)
    }

    fun scheduleAlarms(numNightsRemaining: Int) {
        enableBootReceiver()
        cancelAlarms()

        val reminderTime = getReminderTime()
        if (reminderTime == -1) {
            // No alarm wanted.
            return
        }
        val alarmTimes = getAlarmTimes(reminderTime, numNightsRemaining)

        alarmTimes.forEachIndexed { index, timeUTC ->
            Timber.d("Scheduling alarm for $timeUTC")
            val pendingIntent = PendingIntent.getBroadcast(
                context, index, getAlarmIntent(), PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeUTC, pendingIntent)
        }
    }

    fun cancelAlarms() {
        // There should be at most 10 alarms created with request codes 0 through 9.
        for (requestCode in 0 until 10) {
            alarmManager.cancel(
                PendingIntent.getBroadcast(context, requestCode, getAlarmIntent(), 0)
            )
        }
    }

    private fun getAlarmIntent(): Intent {
        return Intent(context, NotificationPublisher::class.java).apply {
            action = ALARM_ACTION
            putExtra(KEY_NOTIFICATION, createReminderNotification())
        }
    }

    private fun enableBootReceiver() {
        val receiver = ComponentName(context, MasimoBootReceiver::class.java)

        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getAlarmTimes(
        beforeBedTimeOffsetSeconds: Int,
        numNightsRemaining: Int
    ): List<Long> {
        val bedTimeHour = getBedTimeHour()
        val bedTimeMinute = getBedTimeMinute()

        val nextAlarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, bedTimeHour)
            set(Calendar.MINUTE, bedTimeMinute)
            set(Calendar.SECOND, 0)
            add(Calendar.SECOND, beforeBedTimeOffsetSeconds * -1)
        }

        if (Calendar.getInstance().timeInMillis > nextAlarmTime.timeInMillis) {
            // Alarm time already passed in the current day, first alarm will be tomorrow.
            nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmTimes = mutableListOf<Long>()
        for (night in 1..numNightsRemaining) {
            alarmTimes.add(nextAlarmTime.timeInMillis)
            // Add one day.
            nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        return alarmTimes
    }

    private fun getReminderTime(): Int {
        return MasimoSleepPreferences.reminderTime
    }

    private fun getBedTimeHour(): Int {
        return MasimoSleepPreferences.timeHour
    }

    private fun getBedTimeMinute(): Int {
        return MasimoSleepPreferences.timeMinute
    }

    private fun createReminderNotification(): Notification {
        val resultIntent = Intent(context, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            context, 0, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setContentTitle(context.resources.getString(R.string.bedtime_notification_title))
            .setContentText(context.resources.getString(R.string.bedtime_notification_body))
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(resultPendingIntent)
            .build()
    }

    companion object {
        const val ALARM_ACTION = "masimo_sleep_reminder"
    }
}