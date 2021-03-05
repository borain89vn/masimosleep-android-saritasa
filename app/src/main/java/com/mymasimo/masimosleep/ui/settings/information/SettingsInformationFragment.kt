package com.mymasimo.masimosleep.ui.settings.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.ui.settings.SettingsFragmentDirections
import kotlinx.android.synthetic.main.fragment_settings_information.*

class SettingsInformationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_settings_information, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
    }

    private fun setupButtons() {
        feedback_button.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    getString(R.string.support),
                    resources.getString(R.string.feedback_body),
                    resources.getString(R.string.feedback_button),
                    resources.getString(R.string.feedback_email)
                )
            )
        }

        about_button.setOnClickListener {

            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    getString(R.string.about_btn),
                    resources.getString(R.string.about_body),
                    resources.getString(R.string.about_button),
                    resources.getString(R.string.about_url)
                )
            )
        }

        privacy_button.setOnClickListener {

            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    getString(R.string.privacy_policy_btn),
                    resources.getString(R.string.privacy_policy),
                    "",
                    ""
                )
            )
        }

        eula_button.setOnClickListener {

            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    getString(R.string.eula_title),
                    resources.getString(R.string.eula_text),
                    "",
                    ""
                )
            )
        }
    }

    companion object {
        fun newInstance() = SettingsInformationFragment()
    }
}