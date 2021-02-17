package com.mymasimo.masimosleep.ui.pairing.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_scan_failed.*

class ScanFailedFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan_failed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        cancel_button.setOnClickListener {

            dismiss()
        }

        try_again_button.setOnClickListener {
            tryAgainPressed()
        }
    }

    fun tryAgainPressed() {

        this.view?.let {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_scanFailedFragment_to_scanFragment)
        }

    }

    fun dismiss() {

        this.view?.let {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_scanFailedFragment_to_homeFragment)
        }
    }

}