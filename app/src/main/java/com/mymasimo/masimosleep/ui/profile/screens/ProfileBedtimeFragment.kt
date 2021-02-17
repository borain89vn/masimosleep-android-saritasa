package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.alarm.SleepReminderAlarmScheduler
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile_bedtime.*
import kotlinx.android.synthetic.main.fragment_profile_bedtime.submit_button
import javax.inject.Inject

class ProfileBedtimeFragment : Fragment() {

    @Inject lateinit var sleepReminderAlarmScheduler: SleepReminderAlarmScheduler

    private val vm: ProfileViewModel by activityViewModels()

    companion object {
        private val TAG = ProfileBedtimeFragment::class.simpleName

        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
                content: String?,
                isOnBoarding: Boolean = false

        ) = ProfileBedtimeFragment().apply {
            arguments = bundleOf(
                    CONTENT_KEY to content,
                    IS_ON_BOARDING_KEY to isOnBoarding
            )
        }
    }

    private var content: String? = null
    private var isOnBoarding = false
    private lateinit var listener: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        arguments?.let { arg ->
            content = arg.getString(CONTENT_KEY)
            isOnBoarding = arg.getBoolean(IS_ON_BOARDING_KEY)

        } ?: throw IllegalArgumentException()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_bedtime, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    private fun loadViewContent() {

        content?.let {

            time_picker.hour = MasimoSleepPreferences.timeHour
            time_picker.minute = MasimoSleepPreferences.timeMinute
        }

        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        submit_button.text = getString(buttonStrRes)

        this.submit_button.setOnClickListener {
            updateTime()
            sleepReminderAlarmScheduler.scheduleAlarmsAsync()
            listener()
        }
    }

    fun updateTime() {
        val hour = time_picker.hour
        val minute = time_picker.minute

        vm.timeHour.value = hour
        vm.timeMinute.value = minute

        MasimoSleepPreferences.timeHour = hour
        MasimoSleepPreferences.timeMinute = minute

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

}