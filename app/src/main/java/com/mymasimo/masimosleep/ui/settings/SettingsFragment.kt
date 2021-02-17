package com.mymasimo.masimosleep.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.ui.program_report.ProgramReportFragment
import com.mymasimo.masimosleep.ui.settings.device.SettingsDeviceFragment
import com.mymasimo.masimosleep.ui.settings.information.SettingsInformationFragment
import com.mymasimo.masimosleep.ui.settings.profile.SettingsProfileFragment
import com.mymasimo.masimosleep.ui.settings.sensor.SettingsSensorFragment
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    private fun loadViewContent() {

        close_button.setOnClickListener {

            requireView().findNavController().navigateUp()

        }

        buildSettingsUI()

    }

    private fun buildSettingsUI() {
        removeAllFragments()

        addFragment(SettingsSensorFragment.newInstance(), SENSOR_FRAGMENT_TAG)
        addFragment(SettingsDeviceFragment.newInstance(), DEVICE_FRAGMENT_TAG)
        addFragment(SettingsProfileFragment.newInstance(), PROFILE_FRAGMENT_TAG)
        addFragment(SettingsInformationFragment.newInstance(), INFO_FRAGMENT_TAG)

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
            .add(R.id.settings_layout, fragment, tag)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val SENSOR_FRAGMENT_TAG = "SENSOR_FRAGMENT"
        private const val DEVICE_FRAGMENT_TAG = "DEVICE_FRAGMENT"
        private const val PROFILE_FRAGMENT_TAG = "PROFILE_FRAGMENT"
        private const val INFO_FRAGMENT_TAG = "INFO_FRAGMENT"


        private val ALL_FRAGMENT_TAGS = listOf(
            SENSOR_FRAGMENT_TAG,
            DEVICE_FRAGMENT_TAG,
            PROFILE_FRAGMENT_TAG,
            INFO_FRAGMENT_TAG
        )
    }

}