package com.mymasimo.masimosleep.ui.pairing.scan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentScanFailedBinding

class ScanFailedFragment : Fragment(R.layout.fragment_scan_failed) {
    private val viewBinding by viewBinding(FragmentScanFailedBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.cancelButton.setOnClickListener {
            dismiss()
        }

        viewBinding.tryAgainButton.setOnClickListener {
            tryAgainPressed()
        }
    }

    private fun tryAgainPressed() {
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