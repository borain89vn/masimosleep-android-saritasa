package com.mymasimo.masimosleep.ui.welcome.screens

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentWelcomeScreenBinding

class WelcomeScreenFragment : Fragment(R.layout.fragment_welcome_screen) {

    companion object {
        private const val TITLE_KEY = "TITLE"
        private const val IMAGE_KEY = "IMAGE"
        private const val SUBTITLE_KEY = "SUBTITLE"
        private const val CONTENT_KEY = "CONTENT"
        private const val BUTTON_TITLE_KEY = "BUTTON_TITLE"

        fun newInstance(
            title: String?,
            image: Int,
            subTitle: String,
            content: String,
            buttonTitle: String?
        ) = WelcomeScreenFragment().apply {
            arguments = bundleOf(
                TITLE_KEY to title,
                IMAGE_KEY to image,
                SUBTITLE_KEY to subTitle,
                CONTENT_KEY to content,
                BUTTON_TITLE_KEY to buttonTitle
            )
        }
    }

    private var title: String? = null
    private var image: Int = 0
    private lateinit var subTitle: String
    private lateinit var content: String
    private var buttonTitle: String? = null

    private lateinit var listener: () -> Unit
    private val viewBinding by viewBinding(FragmentWelcomeScreenBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arg ->
            title = arg.getString(TITLE_KEY)
            image = arg.getInt(IMAGE_KEY)
            subTitle = arg.getString(SUBTITLE_KEY) as String
            content = arg.getString(CONTENT_KEY) as String
            buttonTitle = arg.getString(BUTTON_TITLE_KEY)
        } ?: throw IllegalArgumentException()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        title?.let {
            viewBinding.titleTextView.text = it
            viewBinding.titleTextView.visibility = View.VISIBLE
        } ?: run {
            viewBinding.titleTextView.visibility = View.INVISIBLE
        }

        viewBinding.contentImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, this.image, null))
        viewBinding.subTitleTextView.text = this.subTitle
        viewBinding.contentTextView.text = this.content

        this.buttonTitle?.let {
            viewBinding.submitButton.text = it
            viewBinding.submitButton.visibility = View.VISIBLE
        } ?: run {
            viewBinding.submitButton.visibility = View.INVISIBLE
        }

        viewBinding.submitButton.setOnClickListener {
            listener()
        }
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}