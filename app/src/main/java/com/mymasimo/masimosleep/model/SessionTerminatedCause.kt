package com.mymasimo.masimosleep.model

import androidx.annotation.StringRes
import com.mymasimo.masimosleep.R

enum class SessionTerminatedCause(@StringRes val title: Int, @StringRes val content: Int, val isRecorded: Boolean) {
    NONE(R.string.session_terminated_dialog_ended_title, R.string.general_error, isRecorded = true),
    SENSOR_OFF_CANCELED(R.string.session_terminated_dialog_canceled_title, R.string.session_terminated_dialog_canceled_des_sensor_disconnection, isRecorded = false),
    SENSOR_OFF_ENDED(R.string.session_terminated_dialog_ended_title, R.string.session_terminated_dialog_ended_des_sensor_disconnection, isRecorded = true),
    SENSOR_DISCONNECTED_CANCELLED(R.string.session_terminated_dialog_canceled_title, R.string.session_terminated_dialog_canceled_des_sensor_disconnection, isRecorded = false),
    SENSOR_DISCONNECTED_ENDED(R.string.session_terminated_dialog_ended_title, R.string.session_terminated_dialog_ended_des_sensor_disconnection, isRecorded = true),
    ENOUGH_SLEEP_ENDED(R.string.session_terminated_dialog_ended_title, R.string.session_terminated_dialog_ended_des_prolonged_session, isRecorded = true);

    companion object {
        private val map = SessionTerminatedCause.values().associateBy { it.name }
        fun fromKey(key: String): SessionTerminatedCause = map[key]
            ?: throw IllegalArgumentException("Invalid key $key")
    }
}