package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProfileNameBinding
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import com.mymasimo.masimosleep.ui.profile.ProfileViewPagerFragment

class ProfileNameFragment : Fragment(R.layout.fragment_profile_name) {

    private val vm: ProfileViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentProfileNameBinding::bind)

    companion object {
        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
            content: String?,
            isOnBoarding: Boolean = false

        ) = ProfileNameFragment().apply {
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
        viewBinding.nameText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateSubmitButton()
            }

        })

        content?.let {
            viewBinding.nameText.setText(it)
        }

        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        viewBinding.submitButton.text = getString(buttonStrRes)

        updateSubmitButton()

        viewBinding.submitButton.setOnClickListener {
            vm.profileName.value = viewBinding.nameText.text.toString()
            MasimoSleepPreferences.name = viewBinding.nameText.text.toString()
            listener()
        }
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    fun updateSubmitButton() {
        viewBinding.submitButton.isEnabled = viewBinding.nameText.text.count() != 0

        if (parentFragment is ProfileViewPagerFragment) {
            (parentFragment as ProfileViewPagerFragment).enablePager(viewBinding.submitButton.isEnabled)
        }
    }
}