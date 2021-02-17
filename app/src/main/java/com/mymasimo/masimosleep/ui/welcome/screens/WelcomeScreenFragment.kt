package com.mymasimo.masimosleep.ui.welcome.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_welcome_screen.*


class WelcomeScreenFragment : Fragment() {

    companion object {
        private val TAG = WelcomeScreenFragment::class.simpleName

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

    private var title : String? = null
    private var image : Int = 0
    private lateinit var subTitle : String
    private lateinit var content : String
    private var buttonTitle : String? = null

    private lateinit var listener: () -> Unit

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()

    }

    private fun loadViewContent() {

        title?.let {
            title_text_view.text = it
            title_text_view.visibility = View.VISIBLE
        } ?: run {
            title_text_view.visibility = View.INVISIBLE
        }

        this.content_image_view.setImageDrawable(resources.getDrawable(this.image,null))

        this.sub_title_text_view.text = this.subTitle

        this.content_text_view.text = this.content

        this.buttonTitle?.let {
            this.submit_button.text = it
            this.submit_button.visibility = View.VISIBLE
        } ?: run {
            this.submit_button.visibility = View.INVISIBLE
        }

        this.submit_button.setOnClickListener {
            listener()
        }
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }
}