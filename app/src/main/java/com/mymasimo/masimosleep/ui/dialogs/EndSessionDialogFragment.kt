package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentEndSessionDialogBinding
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import javax.inject.Inject

class EndSessionDialogFragment : DialogFragment(R.layout.fragment_end_session_dialog) {
    private val viewBinding by viewBinding(FragmentEndSessionDialogBinding::bind)

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
            dialogActionHandler.onEndSessionConfirmationClicked()
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