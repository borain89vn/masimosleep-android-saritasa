package com.mymasimo.masimosleep.data.preferences

import androidx.core.content.edit
import com.masimo.android.common.utility.PreferenceStore
import com.mymasimo.masimosleep.MasimoSleepApp
import java.util.*
import kotlin.collections.ArrayList

private const val PREF_SELECTED_MODULE_ID = "pref.selected.module.id"
private const val PREF_USER_NAME = "pref.user.name"
private const val PREF_USER_BIRTHDATE = "pref.user.birthdate"
private const val PREF_USER_GENDER = "pref.user.gender"
private const val PREF_USER_CONDITIONS = "pref.user.conditions"
private const val PREF_USER_TIME_HOUR = "pref.user.timehour"
private const val PREF_USER_TIME_MIN = "pref.user.timemin"
private const val PREF_USER_REMINDER_TIME = "pref.user.reminder"
private const val PREF_USER_EULA_ACCEPTED = "pref.user.eula.accepted"
private const val PREF_USED_EMULATOR = "pref.used.emulator"

object MasimoSleepPreferences {
    private val store: PreferenceStore = PreferenceStore(MasimoSleepApp.get(), "common_preferences")

    var selectedModuleId: Long
        get() = store.getLong(PREF_SELECTED_MODULE_ID, 0L)
        set(value) {
            if (value == 0L) store.edit { remove(PREF_SELECTED_MODULE_ID) }
            else store.edit { putLong(PREF_SELECTED_MODULE_ID, value) }
        }

    var name: String?
        get() = store.getString(PREF_USER_NAME, null)
        set(value) = store.edit { putString(PREF_USER_NAME, value) }

    var gender: String?
        get() = store.getString(PREF_USER_GENDER, null)
        set(value) = store.edit { putString(PREF_USER_GENDER, value) }

    var birthdate: Long
        get() = store.getLong(PREF_USER_BIRTHDATE, Calendar.getInstance().timeInMillis)
        set(value) = store.edit { putLong(PREF_USER_BIRTHDATE, value) }

    var conditionList: ArrayList<String>?
        get() {
            val conditions = store.getStringSet(PREF_USER_CONDITIONS, null)
            return conditions?.let { ArrayList(it) }
        }
        set(value) = store.edit { putStringSet(PREF_USER_CONDITIONS, value?.toSet()) }

    var timeHour: Int
        get() = store.getInt(PREF_USER_TIME_HOUR, -1)
        set(value) = store.edit { putInt(PREF_USER_TIME_HOUR, value) }

    var timeMinute: Int
        get() = store.getInt(PREF_USER_TIME_MIN, -1)
        set(value) = store.edit { putInt(PREF_USER_TIME_MIN, value) }

    var reminderTime: Int
        get() = store.getInt(PREF_USER_REMINDER_TIME, 0)
        set(value) = store.edit { putInt(PREF_USER_REMINDER_TIME, value) }

    var eulaAccepted: Boolean
        get() = store.getBoolean(PREF_USER_EULA_ACCEPTED, false)
        set(value) = store.edit { putBoolean(PREF_USER_EULA_ACCEPTED, value) }

    var emulatorUsed: Boolean
        get() = store.getBoolean(PREF_USED_EMULATOR, false)
        set(value) = store.edit { putBoolean(PREF_USED_EMULATOR, value) }

}
