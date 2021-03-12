package com.mymasimo.masimosleep.ui.session.sleep_quality_description

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentSleepQualityDescriptionBinding

class SleepQualityDescriptionFragment : Fragment(R.layout.fragment_sleep_quality_description) {
    private val viewBinding by viewBinding(FragmentSleepQualityDescriptionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.closeButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }
    }
}