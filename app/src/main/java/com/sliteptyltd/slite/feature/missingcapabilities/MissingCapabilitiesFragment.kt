package com.sliteptyltd.slite.feature.missingcapabilities

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.RequiresApi
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import com.sliteptyltd.slite.MissingCapabilitiesGraphDirections
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapability.BLUETOOTH_CONNECTIVITY
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapability.BLUETOOTH_PERMISSIONS
import com.sliteptyltd.slite.databinding.FragmentMissingCapabilitiesBinding
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.extensions.hasBluetoothCapabilitiesPermissions
import com.sliteptyltd.slite.utils.extensions.navigateUp
import com.sliteptyltd.slite.utils.extensions.openAppSettings
import com.sliteptyltd.slite.utils.extensions.openBluetoothSettings
import com.sliteptyltd.slite.utils.extensions.restart
import com.sliteptyltd.slite.views.composables.SliteTheme
import com.sliteptyltd.slite.views.statelayout.ErrorStateLayout
import com.sliteptyltd.slite.views.statelayout.StateDetails
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject

class MissingCapabilitiesFragment : Fragment(R.layout.fragment_missing_capabilities) {

    private val binding by viewBinding(FragmentMissingCapabilitiesBinding::bind)
    private val args by navArgs<MissingCapabilitiesFragmentArgs>()
    private val bluetoothService by inject<BluetoothService>()
    private val hasBluetoothPermissions: Boolean @RequiresApi(S) get() = requireContext().hasBluetoothCapabilitiesPermissions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSystemBarsOverlaps()
        setupOnBackPressedCallback()
        binding.root.setContent {
            SliteTheme {
                ErrorStateLayout(
                    StateDetails.Error(
                        drawable = R.drawable.ic_sad_face,
                        subtitle = missingCapabilityDescriptionRes,
                        buttonText = R.string.missing_capabilities_settings_button_text
                    ),
                    ::onOpenSettingsButtonClick
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when {
            args.missingCapability == BLUETOOTH_CONNECTIVITY && bluetoothService.isBluetoothEnabled -> navigateUp()
            args.missingCapability == BLUETOOTH_PERMISSIONS && SDK_INT >= S && hasBluetoothPermissions -> requireActivity().restart()
        }
    }

    private fun onOpenSettingsButtonClick() =
        when (args.missingCapability) {
            BLUETOOTH_PERMISSIONS -> openAppSettings()
            BLUETOOTH_CONNECTIVITY -> openBluetoothSettings()
        }

    private val missingCapabilityDescriptionRes: Int
        get() =
            when (args.missingCapability) {
                BLUETOOTH_CONNECTIVITY -> R.string.missing_capabilities_bluetooth_description
                BLUETOOTH_PERMISSIONS -> R.string.missing_capabilities_bluetooth_permissions_description
            }

    private fun setupOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.updateLayoutParams<MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            CONSUMED
        }
    }

    companion object {

        fun NavController.navigateToMissingCapabilitiesFragment(missingCapability: MissingCapability) =
            navigate(MissingCapabilitiesGraphDirections.actionGlobalToMissingCapabilitiesFragment(missingCapability))
    }
}