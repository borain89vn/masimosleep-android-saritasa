package com.mymasimo.masimosleep.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.ui.welcome.screens.WelcomeScreenFragment
import com.mymasimo.masimosleep.util.navigateSafe
import kotlinx.android.synthetic.main.fragment_welcome_view_pager.view.*

class WelcomeViewPagerFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_welcome_view_pager, container, false)

        val fragmentList = arrayListOf<Fragment>(

                WelcomeScreenFragment.newInstance(
                        null,
                        R.drawable.welcome_0,
                        resources.getString(R.string.welcome_subtitle_0),
                        resources.getString(R.string.welcome_content_0),
                        resources.getString(R.string.next)
                ).apply {
                    setOnButtonClickListener {
                        view.welcome_view_pager.currentItem = view.welcome_view_pager.currentItem + 1
                    }
                },
                WelcomeScreenFragment.newInstance(
                        resources.getString(R.string.welcome_title_1),
                        R.drawable.welcome_1,
                        resources.getString(R.string.welcome_subtitle_1),
                        resources.getString(R.string.welcome_content_1),
                        resources.getString(R.string.next)
                ).apply {
                    setOnButtonClickListener {
                        view.welcome_view_pager.currentItem = view.welcome_view_pager.currentItem + 1
                    }
                },
                WelcomeScreenFragment.newInstance(
                        resources.getString(R.string.welcome_title_2),
                        R.drawable.welcome_2,
                        resources.getString(R.string.welcome_subtitle_2),
                        resources.getString(R.string.welcome_content_2),
                        resources.getString(R.string.welcome_button_2)
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

        view.welcome_view_pager.adapter = adapter
        //val indicator : CircleIndicator3 = findViewById(R.id.welcome_indicator) as CircleIndicator3
        view.welcome_indicator.setViewPager(view.welcome_view_pager)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.skip_button.setOnClickListener {
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