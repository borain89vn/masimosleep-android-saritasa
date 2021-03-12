package com.mymasimo.masimosleep.ui.pairing.pair

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentDevicePairedBinding

class DevicePairedFragment : Fragment(R.layout.fragment_device_paired) {
    private val viewBinding by viewBinding(FragmentDevicePairedBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.submitButton.setOnClickListener {
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