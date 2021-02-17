package com.mymasimo.masimosleep.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.mymasimo.masimosleep.R

class SensorAlreadyConnectedDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(R.string.dialog_sensor_already_connected_content)
            .setTitle(R.string.dialog_sensor_already_connected_title)
            .setPositiveButton(R.string.dialog_sensor_already_connected_positive) { _, _ -> dismiss() }
            .setCancelable(false)

        return builder.create()
    }
}