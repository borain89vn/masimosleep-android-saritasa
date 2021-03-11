package com.mymasimo.masimosleep.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProfileViewPagerBinding
import com.mymasimo.masimosleep.ui.device_onboarding.DeviceOnboardingViewPagerAdapter
import com.mymasimo.masimosleep.ui.profile.screens.*
import com.mymasimo.masimosleep.util.hideKeyboard

class ProfileViewPagerFragment : Fragment(R.layout.fragment_profile_view_pager) {

    private val vm: ProfileViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentProfileViewPagerBinding::bind)

    private val medQuestionIndex: Int = 3
    private val bedTimeIndex: Int = 5


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentList = arrayListOf(
            ProfileNameFragment.newInstance(MasimoSleepPreferences.name, isOnBoarding = true).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            ProfileGenderFragment.newInstance(MasimoSleepPreferences.gender, isOnBoarding = true).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            ProfileBirthdateFragment.newInstance(MasimoSleepPreferences.birthdate, isOnBoarding = true).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            ProfileMedicalQuestionFragment.newInstance(null, isOnBoarding = true).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            ProfileConditionsFragment.newInstance(MasimoSleepPreferences.conditionList, isOnBoarding = true).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            ProfileBedtimeFragment.newInstance("test", isOnBoarding = true).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            ProfileReminderFragment.newInstance(MasimoSleepPreferences.reminderTime, isOnBoarding = true).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            ProfileEULAFragment.newInstance(null).apply {
                setOnButtonClickListener {
                    MasimoSleepPreferences.eulaAccepted = true
                    dismiss()
                }
            }
        )

        val adapter = DeviceOnboardingViewPagerAdapter(
            fragmentList,
            childFragmentManager,
            lifecycle
        )

        viewBinding.viewPager.adapter = adapter

        viewBinding.viewPager.isUserInputEnabled = false

        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageUpdated()
            }
        })

        viewBinding.backButton.setOnClickListener {
            backPressed()
        }
    }

    fun dismiss() {
        vm.printValues()

        this.view?.let {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_profileViewPagerFragment_to_programStartedFragment)
        }
    }

    private fun backPressed() {
        var prevFragmentIndex: Int = viewBinding.viewPager.currentItem - 1
        if (viewBinding.viewPager.currentItem == bedTimeIndex && !vm.hasCondition) {
            prevFragmentIndex = medQuestionIndex
        }

        viewBinding.viewPager.currentItem = prevFragmentIndex
    }

    private fun nextFragment() {
        hideKeyboard()

        var nextFragmentIndex: Int = viewBinding.viewPager.currentItem + 1
        if (viewBinding.viewPager.currentItem == medQuestionIndex && !vm.hasCondition) {
            nextFragmentIndex = bedTimeIndex
        }

        viewBinding.viewPager.currentItem = nextFragmentIndex
    }

    fun pageUpdated() {
        val currentItem = viewBinding.viewPager.currentItem
        if (currentItem == 0) {
            viewBinding.backButton.visibility = View.INVISIBLE
        } else {
            viewBinding.backButton.visibility = View.VISIBLE
        }
    }

    fun enablePager(isEnabled: Boolean) {
        viewBinding.viewPager.isUserInputEnabled = isEnabled
    }

}