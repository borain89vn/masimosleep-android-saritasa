package com.mymasimo.masimosleep

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.mymasimo.masimosleep.ui.home.HomeFragment
import com.mymasimo.masimosleep.ui.session.SessionFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onBackPressed() {
        //Back is disabled only on home  and Session Screen
        val hostFragment = supportFragmentManager.fragments.first()
        val navController = hostFragment.findNavController()
        val currentDestination = (navController.currentDestination as FragmentNavigator.Destination).className
        if (currentDestination != HomeFragment::class.qualifiedName && currentDestination != SessionFragment::class.qualifiedName) {
            navController.popBackStack()
        }
    }
}