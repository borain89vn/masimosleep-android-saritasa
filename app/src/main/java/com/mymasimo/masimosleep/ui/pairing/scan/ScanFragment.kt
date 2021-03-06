package com.mymasimo.masimosleep.ui.pairing.scan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.masimo.android.ui.permission.PermissionHandlerClient
import com.masimo.android.ui.permission.PermissionRationalDialogConfig
import com.masimo.android.ui.permission.PermissionsHandler
import com.mymasimo.masimosleep.BuildConfig
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.base.scheduler.SchedulerProvider
import com.mymasimo.masimosleep.dagger.Injector
import com.mymasimo.masimosleep.databinding.FragmentScanBinding
import com.mymasimo.masimosleep.ui.pairing.PairingViewModel
import com.mymasimo.masimosleep.util.navigateSafe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ScanFragment : Fragment(R.layout.fragment_scan), PermissionHandlerClient {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var disposables: CompositeDisposable

    private val vm: PairingViewModel by activityViewModels { vmFactory }
    private val viewBinding by viewBinding(FragmentScanBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.get().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            cancelButton.setOnClickListener {
                goToScanFailedScreen()
            }

            btnEmulator.isVisible = BuildConfig.ALLOW_EMULATION
            btnEmulator.setOnClickListener {
                vm.connectToEmulator()
            }

            ripplePulse.startPulse()
            ripplePulse2.startPulse()
        }

        vm.pairingFinish.observe(viewLifecycleOwner, { hasFinished ->
            if (hasFinished) {
                view.findNavController().navigateSafe(R.id.action_scanFragment_to_devicePairedFragment)
                vm.onPairingFinishComplete()
            }
        })

        vm.scanTimedOut
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                goToScanFailedScreen()
            }
            .addTo(disposables)

        vm.goToSelectDeviceScreen
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                goToSelectDeviceScreen()
            }
            .addTo(disposables)
    }

    override fun onResume() {
        super.onResume()
        checkBLEPermission()
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun startScanning() {
        vm.startScanningDevices()
    }

    private fun goToScanFailedScreen() {
        vm.stopScanningDevices()
        val navController = requireView().findNavController()
        navController.navigate(R.id.action_scanFragment_to_scanFailedFragment)
    }

    private fun goToSelectDeviceScreen() {
        val navController = requireView().findNavController()
        if (navController.currentDestination?.id == R.id.scanFragment) {
            navController.navigate(R.id.action_scanFragment_to_selectDeviceFragment)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, granted: MutableSet<String>?) {
        if (REQUEST_PERMISSIONS != requestCode) return
        granted ?: return

        if (granted.containsAll(listOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
            requestEnableBluetooth()
        }
    }

    override fun onPermissionDeclined(requestCode: Int, p1: MutableSet<String>?) {
        if (REQUEST_PERMISSIONS != requestCode) return
        // TODO: handle permission declined.
    }

    override fun getPermissionRationaleDialogConfig(
        requestCode: Int,
        granted: MutableSet<String>
    ): PermissionRationalDialogConfig {
        return PermissionRationalDialogConfig.builder()
            .titleRes(R.string.location_permission_rationale_title)
            .messageRes(R.string.location_permission_rationale_message)
            .build()
    }

    private fun checkBLEPermission() {
        val permissionHandler = PermissionsHandler.with(this, this)
        permissionHandler.check(REQUEST_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun requestEnableBluetooth() {
        if (!vm.isBTEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            startScanning()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                startScanning()
            } else {
                goToScanFailedScreen()
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 555
        private const val REQUEST_ENABLE_BT = 101
    }
}