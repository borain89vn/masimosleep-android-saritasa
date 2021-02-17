package com.mymasimo.masimosleep.ui.settings.profile.container

import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.profile.screens.*
import com.mymasimo.masimosleep.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_profile_container.*

class SettingsProfileContainerFragment : Fragment() {

    val args : SettingsProfileContainerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_profile_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    private fun loadViewContent() {

        back_button.setOnClickListener {
            goBack()
        }


        buildUI()

    }

    private fun buildUI() {

        removeAllFragments()

        when(args.fieldType) {

            ProfileFieldType.NAME -> addName()
            ProfileFieldType.GENDER -> addGender()
            ProfileFieldType.BIRTHDATE -> addBirthdate()
            ProfileFieldType.CONDITIONS -> addConditions()
            ProfileFieldType.BEDTIME -> addBedtime()
            ProfileFieldType.REMINDER -> addReminder()

        }



    }


    fun addName() {

        addFragment(ProfileNameFragment.newInstance(MasimoSleepPreferences.name).apply {
            setOnButtonClickListener {
                goBack()
            }
        }, PROFILE_NAME_TAG)

    }

    fun addGender() {

        addFragment(ProfileGenderFragment.newInstance(MasimoSleepPreferences.gender).apply {

            setOnButtonClickListener {
                goBack()
            }

        }, GENDER_TAG)

    }


    fun addBirthdate() {

        addFragment(ProfileBirthdateFragment.newInstance(MasimoSleepPreferences.birthdate).apply {
            setOnButtonClickListener {
                goBack()
            }

        }, BIRTHDATE_TAG)

    }

    fun addConditions() {

        addFragment(ProfileConditionsFragment.newInstance(MasimoSleepPreferences.conditionList).apply {

            setOnButtonClickListener {
                goBack()
            }

        }, CONDITIONS_TAG)

    }

    fun addBedtime() {

        addFragment(ProfileBedtimeFragment.newInstance("").apply {

            setOnButtonClickListener {
                goBack()
            }

        }, BEDTIME_TAG)

    }

    fun addReminder() {
        addFragment(ProfileReminderFragment.newInstance(MasimoSleepPreferences.reminderTime).apply {
            setOnButtonClickListener {
                goBack()
            }
        }, REMINDER_TAG)
    }


    fun goBack() {
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