package com.mymasimo.masimosleep.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.sleepsession.SleepSessionScoreManager
import com.mymasimo.masimosleep.service.BLEConnectionState
import com.mymasimo.masimosleep.service.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_start_button.*
import javax.inject.Inject


class StartButtonFragment : Fragment() {

    @Inject
    lateinit var sleepSessionScoreManager: SleepSessionScoreManager
    @Inject
    lateinit var bleConnectionState: BLEConnectionState
    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var bleConnectionUpdatesDisposable: CompositeDisposable

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val vm: HomeViewModel by activityViewModels { vmFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_start_button, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Button is disabled by default.
        disableStartButton()

        // Observe the program state.
        vm.programState.observe(viewLifecycleOwner) { programState ->
            when (programState) {
                HomeViewModel.ProgramState.NoProgramInProgress -> {
                    // No program in progress, keep the button disabled.

                    // Subscribe to connection changes.
                    bleConnectionUpdatesDisposable.clear()
                    bleConnectionState.currentState
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe { state ->
                            connectionStateChanged(state, programInProgress = false)
                        }
                        .addTo(bleConnectionUpdatesDisposable)
                }
                is HomeViewModel.ProgramState.ProgramInProgress -> {
                    // There is a program in progress, set the button state based on the BLE state.

                    // Subscribe to connection changes.
                    bleConnectionUpdatesDisposable.clear()
                    bleConnectionState.currentState
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe { state ->
                            connectionStateChanged(state, programInProgress = true)
                        }
                        .addTo(bleConnectionUpdatesDisposable)
                }
            }
        }


        start_button.setOnClickListener {
            val nightNumber = vm.getCurrentNight()
            val startedAt = sleepSessionScoreManager.startSession(nightNumber) //changed from 1
            val navController = findNavController(this)
            navController.navigate(
                HomeFragmentDirections.actionHomeFragmentToSessionFragment(startedAt, nightNumber)
            )
        }
    }

    override fun onDestroyView() {
        bleConnectionUpdatesDisposable.clear()
        super.onDestroyView()
    }

    private fun enableStartButton() {
        start_button.isEnabled = true
        start_ripple.startPulse()
    }

    private fun disableStartButton() {
        start_button.isEnabled = false
        start_ripple.stopPulse()
    }

    private fun connectionStateChanged(state: State, programInProgress: Boolean) {
        disableStartButton()

        val buttonTitleId: Int
        val btIconImageId: Int
        val moonImageId: Int
        val sleepSessionColorId: Int
        when (state) {
            State.BLE_DISCONNECTED -> {
                buttonTitleId = R.string.bluetooth_off_status_btn
                btIconImageId = R.drawable.bluetooth_icon
                moonImageId = R.drawable.moon_icon_gray
                sleepSessionColorId = R.color.subtleGray
            }
            State.NO_DEVICE_CONNECTED -> {
                buttonTitleId = R.string.not_connected_status_btn
                btIconImageId = R.drawable.bluetooth_icon
                moonImageId = R.drawable.moon_icon_gray
                sleepSessionColorId = R.color.subtleGray
            }
            State.CONNECTING_TO_DEVICE -> {
                buttonTitleId = R.string.disconnected_status_btn
                btIconImageId = R.drawable.bluetooth_icon
                moonImageId = R.drawable.moon_icon_gray
                sleepSessionColorId = R.color.subtleGray
            }
            State.DEVICE_CONNECTED -> {
                buttonTitleId = R.string.connected_status_btn
                btIconImageId = R.drawable.bluetooth_icon_active
                moonImageId = R.drawable.moon_icon
                sleepSessionColorId = R.color.buttonColor_0
                if (programInProgress) {
                    enableStartButton()
                }
            }
            State.SEARCHING -> {
                buttonTitleId = R.string.searching_status_btn
                btIconImageId = R.drawable.bluetooth_icon
                moonImageId = R.drawable.moon_icon_gray
                sleepSessionColorId = R.color.subtleGray
            }
        }

        connection_button.text = getString(buttonTitleId)
        bt_icon.setImageDrawable(resources.getDrawable(btIconImageId, null))
        moon_icon.setImageDrawable(resources.getDrawable(moonImageId, null))
        sleep_session_label.setTextColor(resources.getColor(sleepSessionColorId, null))

        connection_button.setOnClickListener {
            if (state != State.DEVICE_CONNECTED) {
                findNavController().navigate(R.id.action_homeFragment_to_setUpDeviceDialogFragment)
            }
        }
    }

    companion object {
        fun newInstance(): StartButtonFragment {
            return StartButtonFragment()
        }
    }

}