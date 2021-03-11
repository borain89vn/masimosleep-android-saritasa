package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSensorDisconnectedDialogBinding
import com.mymasimo.masimosleep.service.DeviceException
import com.mymasimo.masimosleep.service.DeviceExceptionHandler
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SensorDisconnectedDialogFragment : SelfDismissDialogFragment(R.layout.fragment_sensor_disconnected_dialog) {
    private val viewBinding by viewBinding(FragmentSensorDisconnectedDialogBinding::bind)

    @Inject
    lateinit var dialogActionHandler: DialogActionHandler
    @Inject
    lateinit var deviceExceptionHandler: DeviceExceptionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        viewBinding.cancelButton.setOnClickListener {
            dialogActionHandler.onEndSleepSessionClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        deviceExceptionHandler.exceptionUpdates
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({
                if (!it.contains(DeviceException.SENSOR_OFF_PATIENT)) {
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}