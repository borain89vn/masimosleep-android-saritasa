package com.mymasimo.masimosleep.ui.remove_chip

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentRemoveChipBinding
import javax.inject.Inject

class RemoveChipFragment : Fragment(R.layout.fragment_remove_chip) {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: RemoveChipViewModel by viewModels { vmFactory }

    private val args: RemoveChipFragmentArgs by navArgs()
    private val viewBinding by viewBinding(FragmentRemoveChipBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.enableButton.observe(viewLifecycleOwner) { action ->
            viewBinding.submitButton.setOnClickListener {
                if (action.programEnded) {
                    findNavController().navigate(
                        RemoveChipFragmentDirections.actionRemoveChipFragmentToProgramCompletedFragment(
                            endedEarly = false
                        )
                    )
                } else {
                    findNavController().navigate(
                        RemoveChipFragmentDirections.actionRemoveChipFragmentToHomeFragment(
                            defaultSessionId = args.sessionId
                        )
                    )
                }
            }
        }
    }

}