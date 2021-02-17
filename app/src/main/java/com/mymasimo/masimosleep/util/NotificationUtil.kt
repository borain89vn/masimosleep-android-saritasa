package com.mymasimo.masimosleep.util


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.mymasimo.masimosleep.R
import java.util.*

/**
 * Utility class to show app notifications.
 */

internal const val CHANNEL_SYSTEM = "system"
internal const val CHANNEL_ALARM = "alarm"
internal const val CHANNEL_REMINDERS = "reminders"

fun initializeNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val current = nm.notificationChannels

    val requiredChannels = HashSet<String>()
    Collections.addAll(requiredChannels, CHANNEL_ALARM, CHANNEL_SYSTEM, CHANNEL_REMINDERS)

    for (channel in current) {
        val channelId = channel.id

        // if no longer a supported channel, remove it
        if (!requiredChannels.remove(channelId)) {
            nm.deleteNotificationChannel(channelId)
        }
    }

    // whatever's left needs to be created
    if (requiredChannels.contains(CHANNEL_ALARM)) createAlarmsChannel(context, nm)
    if (requiredChannels.contains(CHANNEL_REMINDERS)) createRemindersChannel(context, nm)
    if (requiredChannels.contains(CHANNEL_SYSTEM)) createSystemChannel(context, nm)
}

@RequiresApi(api = Build.VERSION_CODES.O)
private fun createSystemChannel(context: Context, notificationManager: NotificationManager) {
    val system = NotificationChannel(CHANNEL_SYSTEM,
            context.getString(R.string.system_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT)
    system.description = context.getString(R.string.system_channel_description)
    system.setShowBadge(false)
    system.setSound(null, AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build())
    notificationManager.createNotificationChannel(system)
}

@RequiresApi(api = Build.VERSION_CODES.O)
private fun createAlarmsChannel(context: Context, notificationManager: NotificationManager) {
    val alarms = NotificationChannel(CHANNEL_ALARM,
            context.getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH)
    alarms.description = context.getString(R.string.alarm_channel_description)
    alarms.setShowBadge(true)
    alarms.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build())
    notificationManager.createNotificationChannel(alarms)
}

@RequiresApi(api = Build.VERSION_CODES.O)
private fun createRemindersChannel(context: Context, notificationManager: NotificationManager) {
    val alarms = NotificationChannel(CHANNEL_REMINDERS,
        context.getString(R.string.reminders_channel_name),
        NotificationManager.IMPORTANCE_HIGH)
    alarms.description = context.getString(R.string.reminders_channel_description)
    alarms.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build())
    notificationManager.createNotificationChannel(alarms)
}