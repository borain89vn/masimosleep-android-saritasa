package com.mymasimo.masimosleep.ui.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences

class NavigationFragment : Fragment(R.layout.fragment_navigation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MasimoSleepPreferences.eulaAccepted) {
            findNavController().navigate(R.id.homeFragment)
        } else {
            findNavController().navigate(R.id.welcomeViewPagerFragment)
        }
    }
}
