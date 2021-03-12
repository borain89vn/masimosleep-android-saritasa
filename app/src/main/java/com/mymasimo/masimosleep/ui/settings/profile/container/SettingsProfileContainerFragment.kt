package com.mymasimo.masimosleep.ui.settings.profile.container

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentSettingsProfileContainerBinding
import com.mymasimo.masimosleep.ui.profile.screens.*

class SettingsProfileContainerFragment : Fragment(R.layout.fragment_settings_profile_container) {

    val args: SettingsProfileContainerFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentSettingsProfileContainerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.backButton.setOnClickListener {
            goBack()
        }
        buildUI()
    }

    private fun buildUI() {
        removeAllFragments()

        when (args.fieldType) {
            ProfileFieldType.NAME -> addName()
            ProfileFieldType.GENDER -> addGender()
            ProfileFieldType.BIRTHDATE -> addBirthdate()
            ProfileFieldType.CONDITIONS -> addConditions()
            ProfileFieldType.BEDTIME -> addBedtime()
            ProfileFieldType.REMINDER -> addReminder()
        }
    }


    private fun addName() {
        addFragment(ProfileNameFragment.newInstance(MasimoSleepPreferences.name).apply {
            setOnButtonClickListener {
                goBack()
            }
        }, PROFILE_NAME_TAG)
    }

    private fun addGender() {
        addFragment(ProfileGenderFragment.newInstance(MasimoSleepPreferences.gender).apply {
            setOnButtonClickListener {
                goBack()
            }
        }, GENDER_TAG)
    }


    private fun addBirthdate() {
        addFragment(ProfileBirthdateFragment.newInstance(MasimoSleepPreferences.birthdate).apply {
            setOnButtonClickListener {
                goBack()
            }
        }, BIRTHDATE_TAG)
    }

    private fun addConditions() {
        addFragment(ProfileConditionsFragment.newInstance(MasimoSleepPreferences.conditionList).apply {
            setOnButtonClickListener {
                goBack()
            }
        }, CONDITIONS_TAG)
    }

    private fun addBedtime() {
        addFragment(ProfileBedtimeFragment.newInstance("").apply {
            setOnButtonClickListener {
                goBack()
            }
        }, BEDTIME_TAG)
    }

    private fun addReminder() {
        addFragment(ProfileReminderFragment.newInstance(MasimoSleepPreferences.reminderTime).apply {
            setOnButtonClickListener {
                goBack()
            }
        }, REMINDER_TAG)
    }


    private fun goBack() {
        requireView().findNavController().navigateUp()
    }

    private fun removeAllFragments() {
        ALL_FRAGMENT_TAGS.forEach { tag ->
            parentFragmentManager.findFragmentByTag(tag)?.let { fragment ->
                parentFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        parentFragmentManager.beginTransaction()
            .add(R.id.profile_layout, fragment, tag)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val PROFILE_NAME_TAG = "NAME_FRAGMENT"
        private const val GENDER_TAG = "GENDER"
        private const val CONDITIONS_TAG = "CONDITIONS"
        private const val BIRTHDATE_TAG = "BIRTHDATE"
        private const val BEDTIME_TAG = "BEDTIME"
        private const val REMINDER_TAG = "REMINDER"

        private val ALL_FRAGMENT_TAGS = listOf(
            PROFILE_NAME_TAG,
            GENDER_TAG,
            CONDITIONS_TAG,
            BIRTHDATE_TAG,
            BEDTIME_TAG,
            REMINDER_TAG
        )
    }
}