package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import kotlinx.android.synthetic.main.fragment_end_program_dialog.*
import javax.inject.Inject

class EndProgramDialogFragment : DialogFragment() {

    @Inject lateinit var dialogActionHandler: DialogActionHandler

    val args: EndProgramDialogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_end_program_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog_text.text = getString(R.string.end_sleep_checkup_content, NUM_OF_NIGHTS - args.nightNumber)

        cancel_button.setOnClickListener {
            dismiss()
        }

        action_button.setOnClickListener {
            dismiss()
            dialogActionHandler.onEndProgramConfirmationClicked()
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