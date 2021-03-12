package com.mymasimo.masimosleep.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentWelcomeViewPagerBinding
import com.mymasimo.masimosleep.ui.welcome.screens.WelcomeScreenFragment
import com.mymasimo.masimosleep.util.navigateSafe

class WelcomeViewPagerFragment : Fragment(R.layout.fragment_welcome_view_pager) {
    private val viewBinding by viewBinding(FragmentWelcomeViewPagerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentList = arrayListOf<Fragment>(
            WelcomeScreenFragment.newInstance(
                title = null,
                image = R.drawable.welcome_0,
                subTitle = resources.getString(R.string.welcome_subtitle_0),
                content = resources.getString(R.string.welcome_content_0),
                buttonTitle = resources.getString(R.string.next)
            ).apply {
                setOnButtonClickListener {
                    viewBinding.welcomeViewPager.currentItem = viewBinding.welcomeViewPager.currentItem + 1
                }
            },
            WelcomeScreenFragment.newInstance(
                title = resources.getString(R.string.welcome_title_1),
                image = R.drawable.welcome_1,
                subTitle = resources.getString(R.string.welcome_subtitle_1),
                content = resources.getString(R.string.welcome_content_1),
                buttonTitle = resources.getString(R.string.next)
            ).apply {
                setOnButtonClickListener {
                    viewBinding.welcomeViewPager.currentItem = viewBinding.welcomeViewPager.currentItem + 1
                }
            },
            WelcomeScreenFragment.newInstance(
                title = resources.getString(R.string.welcome_title_2),
                image = R.drawable.welcome_2,
                subTitle = resources.getString(R.string.welcome_subtitle_2),
                content = resources.getString(R.string.welcome_content_2),
                buttonTitle = resources.getString(R.string.welcome_button_2)
            ).apply {
                setOnButtonClickListener {
                    dismiss()
                }
            }
        )

        val adapter = WelcomeViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        viewBinding.welcomeViewPager.adapter = adapter
        //val indicator : CircleIndicator3 = findViewById(R.id.welcome_indicator) as CircleIndicator3
        viewBinding.welcomeIndicator.setViewPager(viewBinding.welcomeViewPager)

        viewBinding.skipButton.setOnClickListener {
            skipPressed()
        }
    }

    private fun skipPressed() {
        dismiss()
    }

    fun dismiss() {
        findNavController().navigateSafe(R.id.action_welcomeViewPagerFragment_to_profileViewPagerFragment)
    }
}