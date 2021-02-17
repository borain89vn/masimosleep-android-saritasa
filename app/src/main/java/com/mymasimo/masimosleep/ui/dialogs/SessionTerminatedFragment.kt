package com.mymasimo.masimosleep.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.repository.SessionTerminatedRepository
import com.mymasimo.masimosleep.data.repository.SurveyRepository
import com.mymasimo.masimosleep.model.SessionTerminatedCause
import com.mymasimo.masimosleep.ui.waking.survey.SurveyFragmentArgs
import kotlinx.android.synthetic.main.fragment_session_terminated_dialog.*
import javax.inject.Inject

class SessionTerminatedFragment : DialogFragment() {
    @Inject lateinit var surveyRepository: SurveyRepository
    @Inject lateinit var sessionTerminatedRepository: SessionTerminatedRepository
    @Inject lateinit var schedulerProvider: SchedulerProvider

    val args: SessionTerminatedFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_session_terminated_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancel_button.setOnClickListener {
            args.sessionTerminatedEntity?.let {
                if (it.sessionId == null || it.sessionId == null || it.night == null || !it.recorded) {
                    requireParentFragment().findNavController().navigate(R.id.homeFragment, null, NavOptions.Builder().setLaunchSingleTop(true).build())
                } else {
                    requireParentFragment().findNavController().navigate(
                            R.id.surveyFragment,
                            SurveyFragmentArgs(it.sessionId, it.night).toBundle())
                }
            }

            dismiss()
        }

        args.sessionTerminatedEntity?.cause?.let {
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
        title_text.text = getString(cause.title)
        dialog_text.text = getString(cause.content)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}