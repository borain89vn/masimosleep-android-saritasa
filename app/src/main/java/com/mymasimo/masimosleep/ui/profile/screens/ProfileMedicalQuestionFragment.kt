package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentProfileMedicalQuestionBinding
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel

class ProfileMedicalQuestionFragment : Fragment(R.layout.fragment_profile_medical_question) {

    private val vm: ProfileViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentProfileMedicalQuestionBinding::bind)

    companion object {
        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
            content: Boolean?,
            isOnBoarding: Boolean = false

        ) = ProfileMedicalQuestionFragment().apply {
            arguments = bundleOf(
                CONTENT_KEY to content,
                IS_ON_BOARDING_KEY to isOnBoarding
            )
        }
    }

    private var content: Boolean? = null
    private var isOnBoarding = false
    private lateinit var listener: () -> Unit


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = null
        arguments?.let { args ->
            isOnBoarding = args.getBoolean(IS_ON_BOARDING_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        viewBinding.submitButton.text = getString(buttonStrRes)

        viewBinding.yesButton.setOnClickListener {
            clearAll()
            viewBinding.yesButton.isSelected = !viewBinding.yesButton.isSelected
            this.content = true
            updateSubmitButton()
        }

        viewBinding.noButton.setOnClickListener {
            clearAll()
            viewBinding.noButton.isSelected = !viewBinding.noButton.isSelected
            this.content = false
            updateSubmitButton()
        }

        viewBinding.submitButton.setOnClickListener {
            this.content?.let { hasCondition ->
                vm.hasCondition = hasCondition
            }

            listener()
        }

        updateSubmitButton()
    }

    private fun clearAll() {
        viewBinding.yesButton.isSelected = false
        viewBinding.noButton.isSelected = false
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