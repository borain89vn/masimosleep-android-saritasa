package com.mymasimo.masimosleep.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.repository.SessionTerminatedRepository
import com.mymasimo.masimosleep.data.repository.SurveyRepository
import com.mymasimo.masimosleep.databinding.FragmentSessionTerminatedDialogBinding
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.ui.waking.survey.SurveyFragmentArgs
import javax.inject.Inject

class SessionTerminatedFragment : DialogFragment(R.layout.fragment_session_terminated_dialog) {
    @Inject
    lateinit var surveyRepository: SurveyRepository
    @Inject
    lateinit var sessionTerminatedRepository: SessionTerminatedRepository
    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    val args: SessionTerminatedFragmentArgs by navArgs()

    private val viewBinding by viewBinding(FragmentSessionTerminatedDialogBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.cancelButton.setOnClickListener {
            args.sessionTerminatedEntity.let {
                if (it.sessionId == null || it.night == null || !it.recorded) {
                    requireParentFragment().findNavController().navigate(R.id.homeFragment, null, NavOptions.Builder().setLaunchSingleTop(true).build())
                } else {
                    requireParentFragment().findNavController().navigate(
                        R.id.surveyFragment,
                        SurveyFragmentArgs(it.sessionId, it.night).toBundle()
                    )
                }
            }

            dismiss()
        }

        args.sessionTerminatedEntity.cause?.let {
            setupView(it)
        } ?: kotlin.run {
            setupView(SessionTerminatedCause.NONE)
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        sessionTerminatedRepository.updatedHandledLatestTerminatedModel()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe()
        super.onDismiss(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionTerminatedRepository.updatedHandledLatestTerminatedModel()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe()
    }

    private fun setupView(cause: SessionTerminatedCause) {
        viewBinding.titleText.text = getString(cause.title)
        viewBinding.dialogText.text = getString(cause.content)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}