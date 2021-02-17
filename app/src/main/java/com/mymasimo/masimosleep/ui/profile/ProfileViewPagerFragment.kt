package com.mymasimo.masimosleep.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.ui.device_onboarding.DeviceOnboardingViewPagerAdapter
import com.mymasimo.masimosleep.ui.profile.screens.*
import com.mymasimo.masimosleep.util.hideKeyboard
import kotlinx.android.synthetic.main.fragment_profile_view_pager.*
import kotlinx.android.synthetic.main.fragment_profile_view_pager.view.*

class ProfileViewPagerFragment : Fragment() {

    private val vm: ProfileViewModel by activityViewModels()

    private val medQuestionIndex: Int = 3
    private val bedTimeIndex: Int = 5

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_view_pager, container, false)

        val fragmentList = arrayListOf<Fragment>(
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

        view.view_pager.adapter = adapter

        view.view_pager.isUserInputEnabled = false

        view.view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageUpdated()
            }
        })



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        back_button.setOnClickListener {
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

    fun backPressed() {
        this.view?.let { view ->

            var prevFragmentIndex: Int = view.view_pager.currentItem - 1
            if (view.view_pager.currentItem == bedTimeIndex && !vm.hasCondition) {
                prevFragmentIndex = medQuestionIndex
            }

            view.view_pager.currentItem = prevFragmentIndex
        }
    }

    fun nextFragment() {

        hideKeyboard()

        this.view?.let { view ->

            var nextFragmentIndex: Int = view.view_pager.currentItem + 1
            if (view.view_pager.currentItem == medQuestionIndex && !vm.hasCondition) {
                nextFragmentIndex = bedTimeIndex
            }

            view.view_pager.currentItem = nextFragmentIndex
        }
    }

    fun pageUpdated() {
        this.view?.let { view ->
            val currentItem = view.view_pager.currentItem
            val adapter = view.view_pager.adapter as DeviceOnboardingViewPagerAdapter
            val lastPageIndex = adapter.itemCount - 1
            if (currentItem == 0) {
                view.back_button.visibility = View.INVISIBLE
            } else {
                view.back_button.visibility = View.VISIBLE
            }

        }
    }

    fun enablePager(isEnabled: Boolean) {
        requireView().view_pager.isUserInputEnabled = isEnabled
    }

}