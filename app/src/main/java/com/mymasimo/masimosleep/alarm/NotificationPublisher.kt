package com.mymasimo.masimosleep.alarm

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class NotificationPublisher : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SleepReminderAlarmScheduler.ALARM_ACTION) return
        Timber.i("Received broadcast for alarm, firing notification")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        intent.getParcelableExtra<Notification>(KEY_NOTIFICATION)?.let { notification ->
            notification.`when` = System.currentTimeMillis()
            notificationManager.notify(0, notification)
        }
    }

    companion object {
        const val KEY_NOTIFICATION = "masimo_sleep_notification"
    }
}