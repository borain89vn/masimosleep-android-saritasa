package com.mymasimo.masimosleep.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentChipDisconnectedDialogBinding
import com.mymasimo.masimosleep.service.BLEConnectionState
import com.mymasimo.masimosleep.service.State
import com.mymasimo.masimosleep.ui.dialogs.util.DialogActionHandler
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ChipDisconnectedDialogFragment : SelfDismissDialogFragment(R.layout.fragment_chip_disconnected_dialog) {
    private val viewBinding by viewBinding(FragmentChipDisconnectedDialogBinding::bind)

    @Inject
    lateinit var dialogActionHandler: DialogActionHandler
    @Inject
    lateinit var bleConnectionState: BLEConnectionState

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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onResume() {
        super.onResume()
        bleConnectionState.currentState
            .map { state -> state == State.DEVICE_CONNECTED }
            .doOnNext { state -> Timber.d("BLE connection state changed to $state") }
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ isConnected ->
                if (isConnected) {
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