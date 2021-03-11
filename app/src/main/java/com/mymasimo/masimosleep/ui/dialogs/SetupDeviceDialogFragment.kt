package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentSetupDeviceDialogBinding
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import javax.inject.Inject

class SetupDeviceDialogFragment : DialogFragment(R.layout.fragment_setup_device_dialog) {

    private val viewBinding by viewBinding(FragmentSetupDeviceDialogBinding::bind)

    @Inject
    lateinit var dialogActionHandler: DialogActionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.cancelButton.setOnClickListener {
            dismiss()
        }

        viewBinding.actionButton.setOnClickListener {
            dialogActionHandler.onSetUpDeviceNowClicked()
        }

        viewBinding.titleText.text = getString(R.string.greeting_name_text, MasimoSleepPreferences.name)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}