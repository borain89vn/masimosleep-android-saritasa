package com.mymasimo.masimosleep.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import javax.inject.Inject

class ConfirmReplaceSensorDialogFragment : DialogFragment() {
    @Inject lateinit var dialogActionHandler: DialogActionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(R.string.dialog_confirm_replace_sensor_content)
            .setTitle(R.string.dialog_confirm_replace_sensor_title)
            .setPositiveButton(R.string.dialog_confirm_replace_sensor_positive) { _, _ -> dialogActionHandler.onConfirmReplaceSensorClicked() }
            .setNegativeButton(R.string.dialog_confirm_replace_sensor_negative) { _, _ -> dismiss() }

        return builder.create()
    }
}