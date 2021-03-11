package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentCancelSessionDialogBinding
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import javax.inject.Inject


class CancelSessionDialogFragment : DialogFragment(R.layout.fragment_cancel_session_dialog) {
    private val viewBinding by viewBinding(FragmentCancelSessionDialogBinding::bind)

    @Inject
    lateinit var dialogActionHandler: DialogActionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.actionButton.setOnClickListener {
            dismiss()
        }

        viewBinding.actionButton.setOnClickListener {
            dialogActionHandler.onCancelSessionConfirmationClicked()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}