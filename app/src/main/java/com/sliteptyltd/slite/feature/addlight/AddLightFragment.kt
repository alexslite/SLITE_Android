package com.sliteptyltd.slite.feature.addlight

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.app.Activity.LOCATION_SERVICE
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.FragmentAddLightBinding
import com.sliteptyltd.slite.feature.addlight.availableslites.AvailableSliteDetails
import com.sliteptyltd.slite.feature.addlight.availableslites.AvailableSlitesList
import com.sliteptyltd.slite.utils.Constants.Connectivity.MIN_SDK_31_BLUETOOTH_PERMISSIONS
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.extensions.hasBluetoothCapabilitiesPermissions
import com.sliteptyltd.slite.utils.extensions.navigateUp
import com.sliteptyltd.slite.utils.extensions.openAppSettings
import com.sliteptyltd.slite.utils.extensions.openBluetoothSettings
import com.sliteptyltd.slite.utils.extensions.openLocationSettings
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import com.sliteptyltd.slite.views.statelayout.StateDetails
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("MissingPermission")
class AddLightFragment : Fragment(R.layout.fragment_add_light) {

    private val binding by viewBinding(FragmentAddLightBinding::bind)
    private val viewModel by viewModel<AddLightViewModel>()
    private val dialogHandler by inject<DialogHandler>()
    private val bluetoothService by inject<BluetoothService>()
    private val requestStartBluetoothLauncher = initStartBluetoothContractLauncher()
    private val hasLocationPermission: Boolean get() = getHasFineLocationPermission()
    private val hasBluetoothPermissions: Boolean @RequiresApi(S) get() = requireContext().hasBluetoothCapabilitiesPermissions
    private val requestBluetoothPermissionsLauncher = initBluetoothPermissionsLauncher()
    private val requestFineLocationPermissionLauncher = initRequestFineLocationPermissionLauncher()
    private var availableSlitesList by mutableStateOf(listOf<AvailableSliteDetails>())
    private val availableBluetoothDevices = mutableListOf<AvailableSliteDetails>()
    private var isSliteNameBottomSheetShowing: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSystemBarsOverlaps()
        initListeners()
        setupStateLayout()
        showLoadingState()
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.shouldCheckPermissions) {
            if (viewModel.shouldContinueScanning) scanForNearbySliteDevices()
            return
        }

        handlePermissions()
    }

    override fun onPause() {
        super.onPause()

        viewModel.shouldContinueScanning = shouldContinueScanning()
        if (viewModel.shouldContinueScanning) {
            bluetoothService.stopScan()
        }
    }

    private fun initListeners() {
        binding.backIB.setOnClickListener {
            navigateUp()
        }

        binding.stateLayout.setOnActionButtonClick {
            viewModel.shouldCheckBluetoothAvailability = true
            handlePermissions()
        }

        bluetoothService.setOnScanSuccessListener { sliteDevice ->
            val deviceId: Int = sliteDevice.address.hashCode()
            if (deviceId !in availableBluetoothDevices.map { it.id }) {
                availableBluetoothDevices.add(AvailableSliteDetails(deviceId, sliteDevice.name, sliteDevice.address))
            }
        }
    }

    private fun setupStateLayout() {
        binding.stateLayout.setLoadingStateComposable {
            AddLightLoadingStateLayout()
        }

        binding.stateLayout.setSuccessStateComposable {
            AvailableSlitesList(availableSlites = availableSlitesList, ::onAvailableSliteClick)
        }
    }

    private fun showErrorState() {
        binding.stateLayout.setState(errorStateDetails)
    }

    private fun showLoadingState() {
        binding.stateLayout.setState(StateDetails.Loading)
    }

    private fun onAvailableSliteClick(defaultSliteName: String, sliteAddress: String) {
        if (!isSliteNameBottomSheetShowing) {
            isSliteNameBottomSheetShowing = true
            dialogHandler.showNameLightDialog(
                requireContext(),
                defaultSliteName,
                getString(R.string.name_slite_title),
                getString(R.string.name_slite_confirm_button_text),
                onNameConfirmed = { sliteName -> navigateToSettingUpFlow(sliteName, sliteAddress) },
                onDismiss = { isSliteNameBottomSheetShowing = false }
            )
        }
    }

    private fun navigateToSettingUpFlow(sliteName: String, sliteAddress: String) {
        findNavController().navigate(AddLightFragmentDirections.actionAddLightFragmentToSettingUpSliteFragment(sliteName, sliteAddress))
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.updatePadding(top = insets.top, bottom = insets.bottom)
            CONSUMED
        }
    }

    private fun handlePermissions() {
        if (SDK_INT >= S) {
            handleBluetoothPermissions()
        } else {
            handleLocationPermissions()
        }
    }

    @RequiresApi(S)
    private fun handleBluetoothPermissions() {
        if (!hasBluetoothPermissions) {
            requestBluetoothPermissions()
        } else {
            checkBluetoothState()
        }
    }

    private fun handleLocationPermissions() {
        if (!hasLocationPermission) {
            requestFineLocationPermissions()
        } else {
            checkBluetoothState()
        }
    }

    private fun initRequestFineLocationPermissionLauncher(): ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkBluetoothState()
            } else {
                showErrorState()
                dialogHandler.showPermissionsRationaleDialog(
                    requireContext(),
                    getString(R.string.rationale_dialog_location_permissions_title),
                    getString(R.string.rationale_dialog_location_permissions_description)
                )
            }
        }

    private fun requestFineLocationPermissions() {
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            showErrorState()
            dialogHandler.showPermissionsRationaleDialog(
                requireContext(),
                getString(R.string.rationale_dialog_location_permissions_title),
                getString(R.string.rationale_dialog_location_permissions_description),
                ::onOpenSettingsButtonClick
            )
        } else {
            viewModel.shouldCheckPermissions = false
            requestFineLocationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun getHasFineLocationPermission(): Boolean = requireContext().checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

    private fun initBluetoothPermissionsLauncher(): ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val areAllBluetoothPermissionsGranted = it.all { permissionRequestResult -> permissionRequestResult.value }
            if (areAllBluetoothPermissionsGranted) {
                checkBluetoothState()
            } else {
                showErrorState()
                dialogHandler.showPermissionsRationaleDialog(
                    requireContext(),
                    getString(R.string.rationale_dialog_bluetooth_permissions_title),
                    getString(R.string.rationale_dialog_bluetooth_permissions_description)
                )
            }
        }

    @RequiresApi(S)
    private fun requestBluetoothPermissions() {
        if (shouldShowRequestPermissionRationale(BLUETOOTH_SCAN) || shouldShowRequestPermissionRationale(BLUETOOTH_CONNECT)) {
            showErrorState()
            dialogHandler.showPermissionsRationaleDialog(
                requireContext(),
                getString(R.string.rationale_dialog_bluetooth_permissions_title),
                getString(R.string.rationale_dialog_bluetooth_permissions_description),
                ::onOpenSettingsButtonClick
            )
        } else {
            viewModel.shouldCheckPermissions = false
            requestBluetoothPermissionsLauncher.launch(MIN_SDK_31_BLUETOOTH_PERMISSIONS)
        }
    }

    private fun checkBluetoothState() {
        if (shouldAskToEnableGPS()) {
            showErrorState()
            dialogHandler.showPermissionsRationaleDialog(
                requireContext(),
                getString(R.string.add_light_start_location_dialog_title),
                getString(R.string.add_light_start_location_dialog_description),
                onOpenSettingsClick = {
                    viewModel.shouldCheckBluetoothAvailability = true
                    openLocationSettings()
                }
            )
        } else {
            if (bluetoothService.isBluetoothEnabled) {
                scanForNearbySliteDevices()
            } else if (viewModel.shouldCheckBluetoothAvailability) {
                viewModel.shouldCheckBluetoothAvailability = false
                requestStartBluetoothLauncher.launch(Intent(ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun scanForNearbySliteDevices() {
        availableBluetoothDevices.clear()
        showLoadingState()
        bluetoothService.startScan()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(BLE_SCAN_DURATION)
            bluetoothService.stopScan()
            val availableDevices = availableBluetoothDevices.distinctBy { it.id }
            if (availableDevices.isNotEmpty()) {
                availableSlitesList = availableDevices
                binding.stateLayout.setState(StateDetails.Success)
            } else {
                showErrorState()
            }
        }
    }

    private fun initStartBluetoothContractLauncher(): ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                scanForNearbySliteDevices()
            } else {
                showErrorState()
                dialogHandler.showPermissionsRationaleDialog(
                    requireContext(),
                    getString(R.string.add_light_start_bluetooth_dialog_title),
                    getString(R.string.add_light_start_bluetooth_dialog_description),
                    onOpenSettingsClick = {
                        viewModel.shouldCheckBluetoothAvailability = true
                        openBluetoothSettings()
                    }
                )
            }
        }

    private fun onOpenSettingsButtonClick() {
        viewModel.shouldCheckPermissions = true
        openAppSettings()
    }

    private fun shouldAskToEnableGPS(): Boolean =
        SDK_INT < S && !(requireContext().getSystemService(LOCATION_SERVICE) as LocationManager).isProviderEnabled(GPS_PROVIDER)

    private fun shouldContinueScanning(): Boolean =
        if (SDK_INT >= S) {
            hasBluetoothPermissions
        } else {
            hasLocationPermission
        } && bluetoothService.isBluetoothEnabled && bluetoothService.isScanning

    companion object {
        private const val BLE_SCAN_DURATION = 5000L

        private val errorStateDetails = StateDetails.Error(
            R.drawable.ic_sad_face,
            subtitle = R.string.add_light_bluetooth_searching_error_text,
            buttonText = R.string.add_light_bluetooth_searching_error_retry_button_text
        )
    }
}