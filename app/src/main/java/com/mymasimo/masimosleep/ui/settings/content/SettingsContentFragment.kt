package com.mymasimo.masimosleep.ui.settings.content

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import kotlinx.android.synthetic.main.fragment_settings_content.*

class SettingsContentFragment : Fragment() {

    val args: SettingsContentFragmentArgs by navArgs()

    lateinit var title: String
    lateinit var content: String
    var url: String? = null
    var buttonTitle: String? = null

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_settings_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_button.setOnClickListener {
            requireView().findNavController().navigateUp()
        }

        loadViewContent()
    }

    private fun loadViewContent() {
        title_text_view.text = this.title
        content_text.text = this.content

        //submit_button.visibility = View.VISIBLE
        buttonTitle?.let { buttonTitle ->
            submit_button.text = buttonTitle

        } ?: run {
            submit_button.visibility = View.INVISIBLE
        }

        url?.let { url ->
            submit_button.setOnClickListener {
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