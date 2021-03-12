package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentProfileEulaBinding

class ProfileEULAFragment : Fragment(R.layout.fragment_profile_eula) {
    private val viewBinding by viewBinding(FragmentProfileEulaBinding::bind)

    companion object {
        private const val CONTENT_KEY = "CONTENT"

        fun newInstance(
            content: String?

        ) = ProfileEULAFragment().apply {
            arguments = bundleOf(
                CONTENT_KEY to content
            )
        }
    }

    private var content: String? = null
    private lateinit var listener: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arg ->
            content = arg.getString(CONTENT_KEY)
        } ?: throw IllegalArgumentException()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.scrollView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
            updateSubmitButton(scrollY)
        }

        viewBinding.submitButton.setOnClickListener {
            listener()
        }

        viewBinding.submitButton.isEnabled = false
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun updateSubmitButton(scrollY: Int) {
        if (scrollY > 1000) {
            //It is possible when OnScrollChangeListener is called this view is already destroyed
            viewBinding.submitButton.isEnabled = true
        }
    }
}