package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.constant.NUM_OF_NIGHTS
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentEndProgramDialogBinding
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import javax.inject.Inject

class EndProgramDialogFragment : DialogFragment(R.layout.fragment_end_program_dialog) {
    private val viewBinding by viewBinding(FragmentEndProgramDialogBinding::bind)

    @Inject
    lateinit var dialogActionHandler: DialogActionHandler

    val args: EndProgramDialogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_end_program_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.dialogText.text = getString(R.string.end_sleep_checkup_content, NUM_OF_NIGHTS - args.nightNumber)

        viewBinding.cancelButton.setOnClickListener {
            dismiss()
        }

        viewBinding.actionButton.setOnClickListener {
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