package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.alarm.SleepReminderAlarmScheduler
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile_reminder.*
import kotlinx.android.synthetic.main.fragment_profile_reminder.submit_button
import javax.inject.Inject

class ProfileReminderFragment : Fragment() {

    @Inject lateinit var sleepReminderAlarmScheduler: SleepReminderAlarmScheduler

    private val vm: ProfileViewModel by activityViewModels()

    companion object {
        private val TAG = ProfileReminderFragment::class.simpleName

        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
                content: Int?,
                isOnBoarding: Boolean = false

        ) = ProfileReminderFragment().apply {
            arguments = bundleOf(
                    CONTENT_KEY to content,
                    IS_ON_BOARDING_KEY to isOnBoarding
            )
        }
    }

    private var content: Int? = null
    private var isOnBoarding = false
    private lateinit var listener: () -> Unit
    val durationList: Array<Int> = arrayOf(0, 900, 1800, 2700, 3600, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)

        arguments?.let { arg ->
            content = arg.getInt(CONTENT_KEY)
            isOnBoarding = arg.getBoolean(IS_ON_BOARDING_KEY)

        } ?: throw IllegalArgumentException()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    private fun loadViewContent() {

        view?.let { view ->
            val reminderTimes = resources.getStringArray(R.array.reminder_times)
            val adapter = ArrayAdapter(
                    view.context,
                    R.layout.support_simple_spinner_dropdown_item,
                    reminderTimes)
            reminder_spinner.adapter = adapter

            reminder_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    selectedReminder(5)
                }

                override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedReminder(position)
                }

            }
        }

        var defaultSelection: Int = 0
        if (this.content != null) {
            defaultSelection = durationList.indexOf(this.content!!)
        }

        val buttonStrRes = if (isOnBoarding) R.string.done else R.string.save
        submit_button.text = getString(buttonStrRes)

        reminder_spinner.setSelection(defaultSelection)

        this.submit_button.setOnClickListener {
            val duration: Int = durationList[reminder_spinner.selectedItemPosition]
            vm.reminderDuration.value = duration
            MasimoSleepPreferences.reminderTime = duration
            sleepReminderAlarmScheduler.scheduleAlarmsAsync()
            listener()
        }
    }

    fun selectedReminder(index: Int) {

        val reminderTimes = resources.getStringArray(R.array.reminder_times)

        val duration: Int = durationList[index]
        val stringRep: String = reminderTimes[index]

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

}