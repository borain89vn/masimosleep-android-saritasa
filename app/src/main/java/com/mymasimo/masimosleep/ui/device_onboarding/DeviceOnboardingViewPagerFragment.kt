package com.mymasimo.masimosleep.ui.device_onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentDeviceOnboardingViewPagerBinding
import com.mymasimo.masimosleep.ui.device_onboarding.screens.DeviceOnBoardingBottomScreen
import com.mymasimo.masimosleep.ui.device_onboarding.screens.DeviceOnboardingScreenFragment

class DeviceOnboardingViewPagerFragment : Fragment(R.layout.fragment_device_onboarding_view_pager) {

    private val viewBinding by viewBinding(FragmentDeviceOnboardingViewPagerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentList = arrayListOf<Fragment>(

            DeviceOnboardingScreenFragment.newInstance(
                resources.getString(R.string.device_onboarding_title_0),
                R.raw.onboarding_sleep_step_01,
                resources.getString(R.string.device_onboarding_subtitle_0),
                resources.getString(R.string.device_onboarding_content_0),
                null
            ).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            DeviceOnboardingScreenFragment.newInstance(
                resources.getString(R.string.device_onboarding_title_1),
                R.raw.onboarding_sleep_step_02,
                resources.getString(R.string.device_onboarding_subtitle_1),
                resources.getString(R.string.device_onboarding_content_1),
                null
            ).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            DeviceOnboardingScreenFragment.newInstance(
                resources.getString(R.string.device_onboarding_title_2),
                R.raw.onboarding_sleep_step_03,
                resources.getString(R.string.device_onboarding_subtitle_2),
                resources.getString(R.string.device_onboarding_content_2),
                null
            ).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            DeviceOnboardingScreenFragment.newInstance(
                resources.getString(R.string.device_onboarding_title_3),
                R.raw.onboarding_sleep_step_04,
                resources.getString(R.string.device_onboarding_subtitle_3),
                resources.getString(R.string.device_onboarding_content_3),
                null
            ).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            DeviceOnboardingScreenFragment.newInstance(
                resources.getString(R.string.device_onboarding_title_4),
                R.raw.onboarding_sleep_step_05,
                resources.getString(R.string.device_onboarding_subtitle_4),
                resources.getString(R.string.device_onboarding_content_4),
                null
            ).apply {
                setOnButtonClickListener {
                    nextFragment()
                }
            },
            DeviceOnboardingScreenFragment.newInstance(
                resources.getString(R.string.device_onboarding_title_5),
                R.drawable.pairing_image_6b,
                resources.getString(R.string.device_onboarding_subtitle_5),
                resources.getString(R.string.device_onboarding_content_5),
                resources.getString(R.string.device_onboarding_button_5),
                isVideoGuide = false
            ).apply {
                setOnButtonClickListener {
                    dismiss()
                }
            }
        )

        val adapter = DeviceOnboardingViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        viewBinding.viewPager.adapter = adapter

        buildBottomViewPagerAdapter(fragmentList)

        viewBinding.circleIndicator.setViewPager(viewBinding.viewPager)

        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageUpdated(position)
            }
        })

        viewBinding.backButton.setOnClickListener {
            backPressed()
        }
    }

    fun dismiss() {
        this.view?.let {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_deviceOnboardingViewPagerFragment_to_scanFragment)
        }
    }

    private fun backPressed() {
        viewBinding.viewPager.currentItem = viewBinding.viewPager.currentItem - 1
    }

    private fun skipPressed() {
        val adapter = viewBinding.viewPager.adapter as DeviceOnboardingViewPagerAdapter
        viewBinding.viewPager.currentItem = adapter.itemCount - 1
    }

    private fun nextFragment() {
        viewBinding.viewPager.currentItem = viewBinding.viewPager.currentItem + 1
    }

    fun pageUpdated(selectedPage: Int) {

        viewBinding.viewPagerBottom.setCurrentItem(selectedPage, true)

        if (selectedPage == 0) {
            viewBinding.backButton.visibility = View.INVISIBLE
        } else {
            viewBinding.backButton.visibility = View.VISIBLE
        }
    }

    private fun buildBottomViewPagerAdapter(fragmentList: List<Fragment>) {
        val bottomList = arrayListOf<Fragment>()

        fragmentList.forEachIndexed { i, _ ->
            bottomList.add(
                DeviceOnBoardingBottomScreen.newInstance(
                    if (i == fragmentList.size - 1) resources.getString(R.string.device_onboarding_button_5) else resources.getString(
                        R.string.next
                    )
                ).apply {
                    setOnButtonClickListener(submitListener = { if (i == fragmentList.size - 1) dismiss() else nextFragment() }, skipListener = { skipPressed() })
                })
        }

        val bottomAdapter = DeviceOnboardingViewPagerAdapter(bottomList, requireActivity().supportFragmentManager, lifecycle)
        viewBinding.viewPagerBottom.adapter = bottomAdapter
    }
}