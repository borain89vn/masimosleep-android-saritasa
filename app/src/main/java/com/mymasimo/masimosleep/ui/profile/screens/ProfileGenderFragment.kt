package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile_gender.*

class ProfileGenderFragment : Fragment() {

    private val vm: ProfileViewModel by activityViewModels()

    companion object {
        private val TAG = ProfileGenderFragment::class.simpleName

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_gender, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        content?.let { gender ->
            clearAll()
            if (gender == "male") {
                male_button.isSelected = true
            } else if (gender == "female") {
                female_button.isSelected = true
            } else if (gender == "other") {
                other_button.isSelected = true
            }
            updateSubmitButton()
        }

        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        submit_button.text = getString(buttonStrRes)

        this.female_button.setOnClickListener {
            clearAll()
            female_button.isSelected = !female_button.isSelected
            this.content = "female"
            updateSubmitButton()
        }

        this.male_button.setOnClickListener {
            clearAll()
            male_button.isSelected = !male_button.isSelected
            this.content = "male"
            updateSubmitButton()
        }

        this.other_button.setOnClickListener {
            clearAll()
            other_button.isSelected = !other_button.isSelected
            this.content = "other"
            updateSubmitButton()
        }

        this.submit_button.setOnClickListener {
            vm.gender.value = this.content
            MasimoSleepPreferences.gender = this.content
            listener()
        }

        updateSubmitButton()
    }

    private fun clearAll() {
        female_button.isSelected = false
        male_button.isSelected = false
        other_button.isSelected = false
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun updateSubmitButton() {
        this.content?.let {
            submit_button.isEnabled = true
        } ?: run {
            submit_button.isEnabled = false
        }

    }
}