package com.mymasimo.masimosleep.ui.settings.device

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSettingsDeviceBinding
import com.mymasimo.masimosleep.ui.settings.SettingsFragmentDirections
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SettingsDeviceFragment : Fragment(R.layout.fragment_settings_device) {
    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: SettingsDeviceViewModel by viewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentSettingsDeviceBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.confirmReplaceDevice
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                vm.deleteDevice()
            }
            .addTo(disposables)

        vm.deviceDeleted
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                requireView().findNavController().navigate(R.id.action_settingsFragment_to_scanFragment)
            }
            .addTo(disposables)

        vm.connect.observe(viewLifecycleOwner, { action ->
            requireView().findNavController().navigate(action)
        })

        setupButtons()
    }

    private fun setupButtons() {
        viewBinding.connectButton.setOnClickListener {
            vm.onConnectTap()
        }

        viewBinding.troubleshootButton.setOnClickListener {
            requireView().findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsContentFragment(
                    resources.getString(R.string.troubleshoot_title),
                    resources.getString(R.string.troubleshoot_body),
                    resources.getString(R.string.troubleshoot_button),
                    resources.getString(R.string.troubleshoot_email)
                )
            )

        }

        viewBinding.orderButton.setOnClickListener {
            launchURL(resources.getString(R.string.order_device_url))
        }
    }

    private fun launchURL(url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

    companion object {
        fun newInstance() = SettingsDeviceFragment()
    }
}