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

    @Inject
    lateinit var bleConnectionState: BLEConnectionState
    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_settings_sensor, container, false)

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

    private fun updateUI(state: State) {
        val dotColorId: Int
        val statusTextId: Int
        val statusDescId: Int
        when (state) {
            State.BLE_DISCONNECTED -> {
                dotColorId = R.drawable.gray_dot
                statusTextId = R.string.bluetooth_disabled_status_title
                statusDescId = R.string.bluetooth_disabled_status_description
            }
            State.NO_DEVICE_CONNECTED,
            State.CONNECTING_TO_DEVICE,
            State.SEARCHING -> {
                dotColorId = R.drawable.gray_dot
                statusTextId = R.string.no_sensor_status_title
                statusDescId = R.string.no_sensor_status_description
            }
            State.DEVICE_CONNECTED -> {
                dotColorId = R.drawable.blue_dot
                statusTextId = R.string.connected_status_title
                statusDescId = R.string.connected_status_description
            }
        }

        status_dot.background = resources.getDrawable(dotColorId, null)
        status_text.text = getString(statusTextId)
        status_desc_text.text = getString(statusDescId)
    }

    companion object {
        fun newInstance() = SettingsSensorFragment()
    }

}