package com.mymasimo.masimosleep.ui.settings.information

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentSettingsInformationBinding
import com.mymasimo.masimosleep.ui.settings.SettingsFragmentDirections

class SettingsInformationFragment : Fragment(R.layout.fragment_settings_information) {
    private val viewBinding by viewBinding(FragmentSettingsInformationBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
    }

    private fun setupButtons() {
        viewBinding.feedbackButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    getString(R.string.support),
                    resources.getString(R.string.feedback_body),
                    resources.getString(R.string.feedback_button),
                    resources.getString(R.string.feedback_email)
                )
            )
        }

        viewBinding.aboutButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    getString(R.string.about_btn),
                    resources.getString(R.string.about_body),
                    resources.getString(R.string.about_button),
                    resources.getString(R.string.about_url)
                )
            )
        }

        viewBinding.privacyButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    getString(R.string.privacy_policy_btn),
                    resources.getString(R.string.privacy_policy),
                    "",
                    ""
                )
            )
        }

        viewBinding.eulaButton.setOnClickListener {
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