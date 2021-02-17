package com.mymasimo.masimosleep.ui.settings.sensor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.service.BLEConnectionState
import com.mymasimo.masimosleep.service.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_settings_sensor.*
import javax.inject.Inject


class SettingsSensorFragment : Fragment() {

    @Inject lateinit var bleConnectionState: BLEConnectionState
    @Inject lateinit var schedulerProvider: SchedulerProvider
    @Inject lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_sensor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI(State.BLE_DISCONNECTED)
        bleConnectionState.currentState
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe { state ->
                updateUI(state)
            }
            .addTo(disposables)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun updateUI(state : State) {
        val dotColorID: Int
        val statusText: String
        val statusDescText: String
        when (state) {
            State.BLE_DISCONNECTED -> {
                dotColorID = R.drawable.gray_dot
                statusText = "Bluetooth disabled"
                statusDescText = "Please enable bluetooth to detect your device."
            }
            State.NO_DEVICE_CONNECTED,
            State.CONNECTING_TO_DEVICE,
            State.SEARCHING -> {
                dotColorID = R.drawable.gray_dot
                statusText = "No sensor detected"
                statusDescText = "If you can\'t connect, troubleshoot your device."
            }
            State.DEVICE_CONNECTED -> {
                dotColorID = R.drawable.blue_dot
                statusText = "Connected"
                statusDescText = "Sensor is currently tracking your vitals."
            }
        }

        status_dot.background = resources.getDrawable(dotColorID, null)
        status_text.text = statusText
        status_desc_text.text = statusDescText
    }

    companion object {
        fun newInstance() = SettingsSensorFragment()
    }

}