package com.mymasimo.masimosleep.ui.remove_chip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.dagger.Injector
import kotlinx.android.synthetic.main.fragment_remove_chip.*
import javax.inject.Inject


class RemoveChipFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    private val vm: RemoveChipViewModel by viewModels { vmFactory }

    private val args: RemoveChipFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_remove_chip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.enableButton.observe(viewLifecycleOwner) { action ->
            submit_button.setOnClickListener {
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