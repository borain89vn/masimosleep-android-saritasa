package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import kotlinx.android.synthetic.main.fragment_cancel_session_dialog.*
import javax.inject.Inject


class CancelSessionDialogFragment : DialogFragment() {

    @Inject lateinit var dialogActionHandler: DialogActionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cancel_session_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancel_button.setOnClickListener {
            dismiss()
        }

        action_button.setOnClickListener {
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

    companion object {
        const val CANCEL_CONFIRMED_KEY = "CANCEL_SESSION_CONFIRMED"
    }
}