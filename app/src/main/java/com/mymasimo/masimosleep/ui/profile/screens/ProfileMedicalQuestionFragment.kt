package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile_medical_question.*
import kotlinx.android.synthetic.main.fragment_profile_medical_question.submit_button

class ProfileMedicalQuestionFragment : Fragment() {

    private val vm: ProfileViewModel by activityViewModels()

    companion object {
        private val TAG = ProfileMedicalQuestionFragment::class.simpleName

        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
            content: Boolean?,
            isOnBoarding: Boolean = false

        ) = ProfileMedicalQuestionFragment().apply {
            arguments = bundleOf(
                    CONTENT_KEY to content,
                    IS_ON_BOARDING_KEY to isOnBoarding
            )
        }
    }

    private var content : Boolean? = null
    private var isOnBoarding = false
    private lateinit var listener: () -> Unit


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = null
        arguments?.let { args ->
            isOnBoarding = args.getBoolean(IS_ON_BOARDING_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_medical_question, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadViewContent()
    }

    private fun loadViewContent() {
        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        submit_button.text = getString(buttonStrRes)

        this.yes_button.setOnClickListener {
            clearAll()
            yes_button.isSelected = !yes_button.isSelected
            this.content = true
            updateSubmitButton()
        }

        this.no_button.setOnClickListener {
            clearAll()
            no_button.isSelected = !no_button.isSelected
            this.content = false
            updateSubmitButton()
        }


        this.submit_button.setOnClickListener {

            this.content?.let {hasCondition ->
                vm.hasCondition = hasCondition
            }

            listener()
        }

        updateSubmitButton()
    }

    fun clearAll() {
        yes_button.isSelected = false
        no_button.isSelected = false

    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    fun updateSubmitButton() {

        this.content?.let {
            submit_button.isEnabled = true
        } ?: run {
            submit_button.isEnabled = false
        }

    }
}