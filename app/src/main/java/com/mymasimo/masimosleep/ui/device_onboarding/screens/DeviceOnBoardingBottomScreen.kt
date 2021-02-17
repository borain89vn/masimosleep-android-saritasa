package com.mymasimo.masimosleep.ui.device_onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentDeviceOnboardingBottomScreenBinding

class DeviceOnBoardingBottomScreen : Fragment() {
    private lateinit var binding: FragmentDeviceOnboardingBottomScreenBinding

    private lateinit var submitListener: () -> Unit

    private lateinit var skipListener: () -> Unit

    private var buttonTitle: String?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buttonTitle = arguments?.getString(EXTRA_BUTTON_TITLE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentDeviceOnboardingBottomScreenBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.submitButton) {
            setOnClickListener {
                submitListener()
            }

            text = buttonTitle
        }

        binding.skipButton.visibility = if(buttonTitle == getString(R.string.device_onboarding_button_5)) View.INVISIBLE else View.VISIBLE

        binding.skipButton.setOnClickListener {
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