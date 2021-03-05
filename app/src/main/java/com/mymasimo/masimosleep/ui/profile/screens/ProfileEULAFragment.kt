package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_profile_eula.*

class ProfileEULAFragment : Fragment() {

    companion object {
        private val TAG = ProfileEULAFragment::class.simpleName

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
            content = arg.getString(ProfileEULAFragment.CONTENT_KEY)

        } ?: throw IllegalArgumentException()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_eula, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        scroll_view.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
            updateSubmitButton(scrollY)
        }

        this.submit_button.setOnClickListener {
            listener()
        }

        submit_button.isEnabled = false
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun updateSubmitButton(scrollY: Int) {
        if (scrollY > 1000) {
            //It is possible when OnScrollChangeListener is called this view is already destroyed
            submit_button?.isEnabled = true
        }
    }
}