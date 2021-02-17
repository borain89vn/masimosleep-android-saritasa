package com.mymasimo.masimosleep.ui.device_onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.ui.device_onboarding.screens.DeviceOnBoardingBottomScreen
import com.mymasimo.masimosleep.ui.device_onboarding.screens.DeviceOnboardingScreenFragment
import kotlinx.android.synthetic.main.fragment_device_onboarding_view_pager.*
import kotlinx.android.synthetic.main.fragment_device_onboarding_view_pager.view.*

class DeviceOnboardingViewPagerFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_device_onboarding_view_pager, container, false)

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

        view.view_pager.adapter = adapter

        buildBottomViewPagerAdapter(view, fragmentList)

        view.circle_indicator.setViewPager(view.view_pager)

        view.view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageUpdated(position)
            }
        })

        return view
    }

    private fun buildBottomViewPagerAdapter(view: View, fragmentList: List<Fragment>) {
        val bottomList = arrayListOf<Fragment>()

        fragmentList.forEachIndexed { i, _ ->
            bottomList.add(DeviceOnBoardingBottomScreen.newInstance(if(i == fragmentList.size - 1) resources.getString(R.string.device_onboarding_button_5) else resources.getString(R.string.next)).apply {
                setOnButtonClickListener(submitListener = { if(i == fragmentList.size - 1) dismiss() else nextFragment() }, skipListener = { skipPressed() })
            })
        }

        val bottomAdapter = DeviceOnboardingViewPagerAdapter(bottomList, requireActivity().supportFragmentManager, lifecycle)
        view.view_pager_bottom.adapter = bottomAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back_button.setOnClickListener {
            backPressed()
        }
    }

    fun dismiss() {

        this.view?.let {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_deviceOnboardingViewPagerFragment_to_scanFragment)
        }
    }

    fun backPressed() {
        this.view?.let { view ->
            view.view_pager.currentItem = view.view_pager.currentItem - 1
        }
    }

    fun skipPressed() {
        this.view?.let { view ->
            val adapter = view.view_pager.adapter as DeviceOnboardingViewPagerAdapter
            view.view_pager.currentItem = adapter.itemCount - 1
        }
    }

    fun nextFragment() {
        this.view?.let { view ->
            view.view_pager.currentItem = view.view_pager.currentItem + 1
        }
    }

    fun pageUpdated(selectedPage: Int) {
        this.view?.let { view ->

            view.view_pager_bottom.setCurrentItem(selectedPage, true)

            if (selectedPage == 0) {
                view.back_button.visibility = View.INVISIBLE
            } else {
                view.back_button.visibility = View.VISIBLE
            }
        }
    }

}