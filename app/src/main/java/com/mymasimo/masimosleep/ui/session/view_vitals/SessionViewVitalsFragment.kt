package com.mymasimo.masimosleep.ui.session.view_vitals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentSessionViewVitalsBinding

class SessionViewVitalsFragment : Fragment(R.layout.fragment_session_view_vitals) {

    companion object {
        fun newInstance() = SessionViewVitalsFragment()
    }

    private lateinit var onClickListener: () -> Unit

    private val viewBinding by viewBinding(FragmentSessionViewVitalsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    fun setOnClickListener(listener: () -> Unit) {
        onClickListener = listener
    }

    private fun loadViewContent() {
        viewBinding.vitalsButton.setOnClickListener {
            onClickListener()
        }
    }
}
