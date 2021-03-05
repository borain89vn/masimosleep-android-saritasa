package com.mymasimo.masimosleep.ui.pairing.pair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_device_paired.*

class DevicePairedFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_device_paired, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submit_button.setOnClickListener {
            dismiss()
        }
    }

    fun dismiss() {
        this.view?.let {
            val navController = Navigation.findNavController(it)
            navController.popBackStack(R.id.homeFragment, false)
        }
    }
}