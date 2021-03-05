package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.service.DeviceException
import com.mymasimo.masimosleep.service.DeviceExceptionHandler
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_battery_low_dialog.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BatteryLowDialogFragment : SelfDismissDialogFragment() {

    @Inject
    lateinit var dialogActionHandler: DialogActionHandler
    @Inject
    lateinit var deviceExceptionHandler: DeviceExceptionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_battery_low_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        cancel_button.setOnClickListener {
            dialogActionHandler.onLowBatteryDialogDismissed()
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

    override fun onResume() {
        super.onResume()
        deviceExceptionHandler.exceptionUpdates
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({
                if (!it.contains(DeviceException.LOW_BATTERY)) {
                    requireParentFragment().requireView().findNavController().popBackStack()
                }
            }, {
                it.printStackTrace()
            }).addTo(disposables)
    }

    override fun onPause() {
        disposables.clear()
        super.onPause()
    }
}