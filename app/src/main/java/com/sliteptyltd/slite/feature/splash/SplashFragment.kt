package com.sliteptyltd.slite.feature.splash

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapabilitiesFragment.Companion.navigateToMissingCapabilitiesFragment
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapability.BLUETOOTH_CONNECTIVITY
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapability.BLUETOOTH_PERMISSIONS
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.extensions.hasBluetoothCapabilitiesPermissions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val internalStorageManager by inject<InternalStorageManager>()
    private val bluetoothService by inject<BluetoothService>()
    private val hasBluetoothPermissions: Boolean @RequiresApi(S) get() = requireContext().hasBluetoothCapabilitiesPermissions
    private var navigationJob: Job? = null

    override fun onResume() {
        super.onResume()
        navigationJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_SCREEN_NAVIGATION_DELAY)

            when {
                internalStorageManager.hasUserAddedFirstDevice && SDK_INT >= S && !hasBluetoothPermissions ->
                    findNavController().navigateToMissingCapabilitiesFragment(BLUETOOTH_PERMISSIONS)
                internalStorageManager.hasUserAddedFirstDevice && !bluetoothService.isBluetoothEnabled ->
                    findNavController().navigateToMissingCapabilitiesFragment(BLUETOOTH_CONNECTIVITY)
                internalStorageManager.isOnboardingComplete ->
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeGraph())
                else ->
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToOnboardingGraph())
            }
        }
    }

    override fun onPause() {
        navigationJob?.cancel()
        super.onPause()
    }

    companion object {
        private const val SPLASH_SCREEN_NAVIGATION_DELAY = 1000L
    }
}