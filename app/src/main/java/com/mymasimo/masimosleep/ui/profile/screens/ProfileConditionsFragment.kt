package com.mymasimo.masimosleep.ui.profile.screens

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.data.preferences.MasimoSleepPreferences
import com.mymasimo.masimosleep.databinding.FragmentProfileConditionsBinding
import com.mymasimo.masimosleep.model.SleepCondition
import com.mymasimo.masimosleep.ui.profile.ProfileViewModel

class ProfileConditionsFragment : Fragment(R.layout.fragment_profile_conditions) {

    private val vm: ProfileViewModel by activityViewModels()
    private val viewBinding by viewBinding(FragmentProfileConditionsBinding::bind)

    companion object {
        private const val CONTENT_KEY = "CONTENT"
        private const val IS_ON_BOARDING_KEY = "is_on_boarding_key"

        fun newInstance(
            content: ArrayList<String>?,
            isOnBoarding: Boolean = false

        ) = ProfileConditionsFragment().apply {
            arguments = bundleOf(
                CONTENT_KEY to content,
                IS_ON_BOARDING_KEY to isOnBoarding
            )
        }
    }

    private var content: ArrayList<String>? = null
    private var isOnBoarding = false
    private lateinit var listener: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arg ->
            content = arg.getStringArrayList(CONTENT_KEY)
            isOnBoarding = arg.getBoolean(IS_ON_BOARDING_KEY)
        } ?: throw IllegalArgumentException()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        if (content?.size ?: 0 > 0) {
            initializeSelections()
        }

        updateSubmitButton()

        val buttonStrRes = if (isOnBoarding) R.string.next else R.string.save
        viewBinding.submitButton.text = getString(buttonStrRes)

        viewBinding.condition1Button.setOnClickListener(onClickListener)
        viewBinding.condition2Button.setOnClickListener(onClickListener)
        viewBinding.condition3Button.setOnClickListener(onClickListener)
        viewBinding.condition4Button.setOnClickListener(onClickListener)

        viewBinding.submitButton.setOnClickListener {
            vm.conditionList.value = this.content
            MasimoSleepPreferences.conditionList = this.content
            listener()
        }
    }

    private val onClickListener = View.OnClickListener { v ->
        when (v?.id) {
            R.id.condition_1_button,
            R.id.condition_2_button,
            R.id.condition_3_button,
            R.id.condition_4_button -> {
                updateSelection(v)
            }
        }
    }

    private fun updateSelection(view: View) {
        view.isSelected = !view.isSelected

        if (content == null) content = ArrayList()

        val index = (view.parent as ViewGroup).indexOfChild(view)

        val condition = SleepCondition.values()[index].name

        if (view.isSelected) content?.add(condition)
        else content?.remove(condition)

        updateSubmitButton()
    }

    private fun initializeSelections() {
        if (this.content?.contains(SleepCondition.CONDITION_1.name) == true) {
            viewBinding.condition1Button.isSelected = true
        }

        if (this.content?.contains(SleepCondition.CONDITION_2.name) == true) {
            viewBinding.condition2Button.isSelected = true
        }

        if (this.content?.contains(SleepCondition.CONDITION_3.name) == true) {
            viewBinding.condition3Button.isSelected = true
        }

        if (this.content?.contains(SleepCondition.CONDITION_4.name) == true) {
            viewBinding.condition4Button.isSelected = true
        }
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun updateSubmitButton() {
        viewBinding.submitButton.isEnabled = content != null
    }

}