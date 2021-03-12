package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProfileGenderBinding
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel

class ProfileGenderFragment : Fragment(R.layout.fragment_profile_gender) {

    private val vm: ProfileViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentProfileGenderBinding::bind)

    companion object {
        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
            content: String?,
            isOnBoarding: Boolean = false

        ) = ProfileGenderFragment().apply {
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
        content?.let { gender ->
            clearAll()
            if (gender == "male") {
                viewBinding.maleButton.isSelected = true
            } else if (gender == "female") {
                viewBinding.femaleButton.isSelected = true
            } else if (gender == "other") {
                viewBinding.otherButton.isSelected = true
            }
            updateSubmitButton()
        }

        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        viewBinding.submitButton.text = getString(buttonStrRes)

        viewBinding.femaleButton.setOnClickListener {
            clearAll()
            viewBinding.femaleButton.isSelected = !viewBinding.femaleButton.isSelected
            this.content = "female"
            updateSubmitButton()
        }

        viewBinding.maleButton.setOnClickListener {
            clearAll()
            viewBinding.maleButton.isSelected = !viewBinding.maleButton.isSelected
            this.content = "male"
            updateSubmitButton()
        }

        viewBinding.otherButton.setOnClickListener {
            clearAll()
            viewBinding.otherButton.isSelected = !viewBinding.otherButton.isSelected
            this.content = "other"
            updateSubmitButton()
        }

        viewBinding.submitButton.setOnClickListener {
            vm.gender.value = this.content
            MasimoSleepPreferences.gender = this.content
            listener()
        }

        updateSubmitButton()
    }

    private fun clearAll() {
        viewBinding.femaleButton.isSelected = false
        viewBinding.maleButton.isSelected = false
        viewBinding.otherButton.isSelected = false
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun updateSubmitButton() {
        this.content?.let {
            viewBinding.submitButton.isEnabled = true
        } ?: run {
            viewBinding.submitButton.isEnabled = false
        }

    }
}