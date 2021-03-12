package com.mymasimo.masimosleep.ui.settings.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentSettingsProfileBinding
import com.mymasimo.masimosleep.model.SleepCondition
import com.mymasimo.masimosleep.ui.settings.SettingsFragmentDirections
import com.mymasimo.masimosleep.ui.settings.profile.container.ProfileFieldType
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class SettingsProfileFragment : Fragment(R.layout.fragment_settings_profile) {
    private val viewBinding by viewBinding(FragmentSettingsProfileBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
        setupButtons()
    }

    private fun loadViewContent() {
        viewBinding.nameButton.text = MasimoSleepPreferences.name
        viewBinding.conditionButton.text = generateConditionsString()

        val gender = MasimoSleepPreferences.gender
        var genderString = "Other"
        if (gender == "male") {
            genderString = "Male"
        } else if (gender == "female") {
            genderString = "Female"
        } else if (gender == "other") {
            genderString = getString(R.string.gender_other)
        }

        viewBinding.genderButton.text = genderString

        val dateFormatter = SimpleDateFormat("MMM d, yyyy")

        val dateString = dateFormatter.format(Date(MasimoSleepPreferences.birthdate))
        viewBinding.birthdateButton.text = dateString
        Timber.d("Birthdate: %s", MasimoSleepPreferences.birthdate.toString())

        var adjustedHour = MasimoSleepPreferences.timeHour
        var amPm = "AM"
        var minuteString = MasimoSleepPreferences.timeMinute.toString()

        if (adjustedHour >= 12) {
            amPm = "PM"
        }

        if (adjustedHour > 12) {
            adjustedHour -= 12
        } else if (adjustedHour == 0) {
            adjustedHour = 12
        }


        if (minuteString.count() == 1) {
            minuteString = "0$minuteString"
        }

        viewBinding.bedtimeButton.text = "$adjustedHour:$minuteString $amPm"

        val reminderTime = MasimoSleepPreferences.reminderTime
        var reminderString = "No Reminder"
        if (reminderTime == 3600) {
            reminderString = "1 hour before"
        } else if (reminderTime == 2700) {
            reminderString = "45 minutes before"
        } else if (reminderTime == 1800) {
            reminderString = "30 minutes before"
        } else if (reminderTime == 900) {
            reminderString = "15 minutes before"
        } else if (reminderTime == 0) {
            reminderString = "At bedtime"
        }

        viewBinding.reminderButton.text = reminderString
    }

    private fun generateConditionsString(): String {
        var result = "None"

        val conditionList = MasimoSleepPreferences.conditionList ?: listOf()

        if (conditionList.count() > 0) {
            result = ""
        }

        for (i in 0 until conditionList.count()) {
            val condition = conditionList[i]

            var conditionString = ""
            when (condition) {
                SleepCondition.CONDITION_1.name -> conditionString = getString(SleepCondition.CONDITION_1.resID)
                SleepCondition.CONDITION_2.name -> conditionString = getString(SleepCondition.CONDITION_2.resID)
                SleepCondition.CONDITION_3.name -> conditionString = getString(SleepCondition.CONDITION_3.resID)
                SleepCondition.CONDITION_4.name -> conditionString = getString(SleepCondition.CONDITION_4.resID)
            }

            result += conditionString

            if (i < conditionList.count() - 1) {
                result = "$result, "
            }
        }

        return result
    }

    private fun setupButtons() {
        viewBinding.nameButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsProfileContainerFragment(
                    ProfileFieldType.NAME
                )
            )
        }

        viewBinding.conditionButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsProfileContainerFragment(
                    ProfileFieldType.CONDITIONS
                )
            )
        }

        viewBinding.genderButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsProfileContainerFragment(
                    ProfileFieldType.GENDER
                )
            )
        }

        viewBinding.birthdateButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsProfileContainerFragment(
                    ProfileFieldType.BIRTHDATE
                )
            )
        }

        viewBinding.bedtimeButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsProfileContainerFragment(
                    ProfileFieldType.BEDTIME
                )
            )
        }

        viewBinding.reminderButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsProfileContainerFragment(
                    ProfileFieldType.REMINDER
                )
            )
        }
    }

    companion object {
        fun newInstance() = SettingsProfileFragment()
    }
}