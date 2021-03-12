package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.alarm.SleepReminderAlarmScheduler
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProfileBedtimeBinding
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import javax.inject.Inject

class ProfileBedtimeFragment : Fragment(R.layout.fragment_profile_bedtime) {

    @Inject
    lateinit var sleepReminderAlarmScheduler: SleepReminderAlarmScheduler

    private val vm: ProfileViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentProfileBedtimeBinding::bind)

    companion object {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        content?.let {
            viewBinding.timePicker.hour = MasimoSleepPreferences.timeHour
            viewBinding.timePicker.minute = MasimoSleepPreferences.timeMinute
        }

        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        viewBinding.submitButton.text = getString(buttonStrRes)

        viewBinding.submitButton.setOnClickListener {
            updateTime()
            sleepReminderAlarmScheduler.scheduleAlarmsAsync()
            listener()
        }
    }

    private fun updateTime() {
        val hour = viewBinding.timePicker.hour
        val minute = viewBinding.timePicker.minute

        vm.timeHour.value = hour
        vm.timeMinute.value = minute

        MasimoSleepPreferences.timeHour = hour
        MasimoSleepPreferences.timeMinute = minute
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}