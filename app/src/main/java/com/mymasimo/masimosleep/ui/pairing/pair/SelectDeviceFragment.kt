package com.mymasimo.masimosleep.ui.pairing.pair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentSelectDeviceBinding
import com.mymasimo.masimosleep.ui.pairing.PairingViewModel
import com.mymasimo.masimosleep.ui.pairing.pair.view.Module
import com.mymasimo.masimosleep.ui.pairing.pair.view.ModuleAdapter
import com.mymasimo.masimosleep.util.navigateSafe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SelectDeviceFragment : Fragment() {

    @Inject lateinit var vmFactory: ViewModelProvider.Factory
    @Inject lateinit var schedulerProvider: SchedulerProvider
    @Inject lateinit var disposables: CompositeDisposable

    private val vm: PairingViewModel by activityViewModels { vmFactory }
    private lateinit var bindings: FragmentSelectDeviceBinding

    private val moduleAdapter =
        ModuleAdapter(
                onModuleClickListener = { module ->
                    vm.saveBleModule(module.address)
                })

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        bindings = FragmentSelectDeviceBinding.inflate(inflater, container, false)
        return bindings.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindings.recyclerView.apply {
            adapter = moduleAdapter
            layoutManager = LinearLayoutManager(context)
        }

        bindings.cancelButton.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_selectDeviceFragment_to_scanFailedFragment)
        }

        vm.nearbyBleModules.observe(viewLifecycleOwner) { nearbyModules ->
            moduleAdapter.setModules(nearbyModules.map { dbModule ->
                Module(
                        deviceName = dbModule.type.name,
                        modelType = dbModule.variant.name,
                        serialNumber = dbModule.serialNumber,
                        address = dbModule.address
                )
            })
        }

        vm.goToDevicePairedScreen
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                view.findNavController()
                    .navigateSafe(R.id.action_selectDeviceFragment_to_devicePairedFragment)
            }
            .addTo(disposables)

        vm.startAutomaticConnectCountDown()
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}