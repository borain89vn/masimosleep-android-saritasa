package com.mymasimo.masimosleep.ui.device_onboarding.screens

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentDeviceOnboardingBottomScreenBinding

class DeviceOnBoardingBottomScreen : Fragment(R.layout.fragment_device_onboarding_bottom_screen) {
    private lateinit var submitListener: () -> Unit

    private lateinit var skipListener: () -> Unit

    private var buttonTitle: String? = null
    private val viewBinding by viewBinding(FragmentDeviceOnboardingBottomScreenBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonTitle = arguments?.getString(EXTRA_BUTTON_TITLE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding.submitButton) {
            setOnClickListener {
                submitListener()
            }

            text = buttonTitle
        }

        viewBinding.skipButton.visibility = if (buttonTitle == getString(R.string.device_onboarding_button_5)) View.INVISIBLE else View.VISIBLE

        viewBinding.skipButton.setOnClickListener {
            skipListener()
        }
    }

    fun setOnButtonClickListener(submitListener: () -> Unit, skipListener: () -> Unit) {
        this.submitListener = submitListener
        this.skipListener = skipListener
    }

    companion object {
        private const val EXTRA_BUTTON_TITLE = "extra_button_title"

        fun newInstance(buttonTitle: String) = DeviceOnBoardingBottomScreen().apply {
            arguments = bundleOf(EXTRA_BUTTON_TITLE to buttonTitle)
        }
    }
}