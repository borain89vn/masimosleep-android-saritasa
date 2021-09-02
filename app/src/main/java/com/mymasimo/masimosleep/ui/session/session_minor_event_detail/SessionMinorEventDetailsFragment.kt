package com.mymasimo.masimosleep.ui.session.session_minor_event_detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentMinorEventDetailBinding
import javax.inject.Inject


class SessionMinorEventDetailsFragment : Fragment(R.layout.fragment_minor_event_detail) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: SessionMinorEventsViewModel by viewModels { vmFactory }
    private val args: SessionMinorEventDetailsFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentMinorEventDetailBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.closeButton.setOnClickListener {
            dismiss()
        }

    }

    private fun dismiss() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigateUp()
    }
}