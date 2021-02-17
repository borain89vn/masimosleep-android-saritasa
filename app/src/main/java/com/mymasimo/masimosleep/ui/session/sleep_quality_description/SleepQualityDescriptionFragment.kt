package com.mymasimo.masimosleep.ui.session.sleep_quality_description

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_sleep_quality_description.*


class SleepQualityDescriptionFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep_quality_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        loadViewContent()
    }

    private fun loadViewContent() {

        close_button.setOnClickListener {
           requireView().findNavController().navigateUp()
        }

    }

}