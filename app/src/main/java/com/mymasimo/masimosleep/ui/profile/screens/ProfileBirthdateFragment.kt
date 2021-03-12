package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProfileBirthdateBinding
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import java.util.*

class ProfileBirthdateFragment : Fragment(R.layout.fragment_profile_birthdate) {

    private val vm: ProfileViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentProfileBirthdateBinding::bind)

    companion object {
        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
            content: Long?,
            isOnBoarding: Boolean = false

        ) = ProfileBirthdateFragment().apply {
            arguments = bundleOf(
                CONTENT_KEY to content,
                IS_ON_BOARDING_KEY to isOnBoarding
            )
        }
    }

    private var content: Long? = null
    private var isOnBoarding = false
    private lateinit var listener: () -> Unit
    private var selectedDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arg ->
            content = arg.getLong(CONTENT_KEY)
            isOnBoarding = arg.getBoolean(IS_ON_BOARDING_KEY)

        } ?: throw IllegalArgumentException()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        viewBinding.submitButton.text = getString(buttonStrRes)

        val today = Calendar.getInstance()
        if (!isOnBoarding) {
            today.timeInMillis = this.content!!
            this.selectedDate = today
        }

        viewBinding.datePicker.init(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH),
            object : DatePicker.OnDateChangedListener {
                override fun onDateChanged(p0: DatePicker?, year: Int, month: Int, day: Int) {
                    dateChanged(year, month, day)
                }
            })

        updateSubmitButton()

        viewBinding.submitButton.setOnClickListener {
            this.selectedDate?.let { birthdate ->
                vm.birthdate.value = birthdate
                MasimoSleepPreferences.birthdate = birthdate.timeInMillis
            }

            listener()
        }
    }

    fun dateChanged(year: Int, month: Int, day: Int) {

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)

        this.selectedDate = cal
        updateSubmitButton()

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun updateSubmitButton() {
        this.selectedDate?.let {
            val today = Calendar.getInstance()
            viewBinding.submitButton.isEnabled = today.time.after(it.time)
        } ?: run {
            viewBinding.submitButton.isEnabled = false
        }
    }
}