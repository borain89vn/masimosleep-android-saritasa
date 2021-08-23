package com.mymasimo.masimosleep

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onBackPressed() {
        //disable
        val hostFragment = supportFragmentManager.fragments.first()
        if (hostFragment.findNavController().popBackStack().not()){
            finish()
        }
    }
}