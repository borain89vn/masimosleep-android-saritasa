package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import com.mymasimo.masimosleep.ui.profile.ProfileViewPagerFragment
import kotlinx.android.synthetic.main.fragment_profile_name.*

class ProfileNameFragment : Fragment() {

    private val vm: ProfileViewModel by activityViewModels()

    companion object {
        private val TAG = ProfileNameFragment::class.simpleName

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        name_text.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateSubmitButton()
            }

        })

        content?.let {
            name_text.setText(it)
        }

        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        submit_button.text = getString(buttonStrRes)

        updateSubmitButton()

        this.submit_button.setOnClickListener {
            vm.profileName.setValue(name_text.text.toString())
            MasimoSleepPreferences.name = name_text.text.toString()
            listener()
        }
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    fun updateSubmitButton() {
        submit_button.isEnabled = name_text.text.count() != 0

        if (parentFragment is ProfileViewPagerFragment) {
            (parentFragment as ProfileViewPagerFragment).enablePager(submit_button.isEnabled)
        }
    }
}