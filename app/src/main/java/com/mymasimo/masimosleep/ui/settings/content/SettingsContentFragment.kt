package com.mymasimo.masimosleep.ui.settings.content

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentSettingsContentBinding

class SettingsContentFragment : Fragment(R.layout.fragment_settings_content) {

    val args: SettingsContentFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentSettingsContentBinding::bind)

    lateinit var title: String
    lateinit var content: String
    private var url: String? = null
    private var buttonTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = args.title
        content = args.content
        if (args.url.count() > 0) {
            url = args.url
        }

        if (args.buttonTitle.count() > 0) {
            buttonTitle = args.buttonTitle
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.backButton.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        loadViewContent()
    }

    private fun loadViewContent() {
        viewBinding.titleTextView.text = this.title
        viewBinding.contentText.text = this.content

        when (title.length) {
            getString(R.string.support).length -> {
                viewBinding.contentText.text = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
                viewBinding.contentText.movementMethod = LinkMovementMethod.getInstance()
            }
            else -> {
                viewBinding.contentText.text = content
            }
        }

        //submit_button.visibility = View.VISIBLE
        buttonTitle?.let { buttonTitle ->
            viewBinding.submitButton.text = buttonTitle

        } ?: run {
            viewBinding.submitButton.visibility = View.INVISIBLE
        }

        url?.let { url ->
            viewBinding.submitButton.setOnClickListener {
                launchURL(url)
            }
        }
    }

    private fun launchURL(url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }
}