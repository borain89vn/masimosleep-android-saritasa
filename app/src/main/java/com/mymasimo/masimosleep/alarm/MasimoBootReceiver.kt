package com.mymasimo.masimosleep.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mymasimo.masimosleep.dagger.Injector
import timber.log.Timber
import javax.inject.Inject

class MasimoBootReceiver @Inject constructor() : BroadcastReceiver() {

    @Inject lateinit var sleepReminderAlarmScheduler: SleepReminderAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        Injector.get().inject(this)

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Timber.d("Masimo Boot Receiver triggered, scheduling alarms..")
            sleepReminderAlarmScheduler.scheduleAlarmsAsync()
        }
    }
}
