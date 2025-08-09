package com.sliteptyltd.slite.feature.addlight.setupslite

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.databinding.FragmentSettingUpSliteBinding
import com.sliteptyltd.slite.utils.AnnouncementHandler
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_HUE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_BLACKOUT
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.SliteEventSubscriber
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic
import com.sliteptyltd.slite.utils.extensions.navigateUp
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class SettingUpSliteFragment : Fragment(R.layout.fragment_setting_up_slite) {

    private val binding by viewBinding(FragmentSettingUpSliteBinding::bind)
    private val viewModel by viewModel<SettingUpSliteViewModel>()
    private val args by navArgs<SettingUpSliteFragmentArgs>()
    private val internalStorageManager by inject<InternalStorageManager>()
    private val bluetoothService by inject<BluetoothService>()
    private val announcementHandler by inject<AnnouncementHandler>()
    private val analyticsService by inject<AnalyticsService>()
    private val isLoading = mutableStateOf(true)
    private val sliteEventSubscriber by lazy { initSliteEventSubscriber() }
    private var lightConfiguration: LightConfiguration = initDefaultLightConfiguration()
    private var status = DISCONNECTED
    private var isLightConfigurationReady = false
    private var isLightModeReady = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setContent {
            SettingUpSliteLayout(isLoading = isLoading.value)
        }
        bluetoothService.addDeviceSubscriber(args.sliteAddress, sliteEventSubscriber)
        bluetoothService.connect(args.sliteAddress)
    }

    private fun initSliteEventSubscriber(): SliteEventSubscriber = object : SliteEventSubscriber() {
        override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
            viewLifecycleOwner.lifecycleScope.launch {
                if (isConnected) {
                    bluetoothService.readSliteOutputValue(address)
                } else {
                    announcementHandler.showWarningAnnouncement(
                        requireActivity(),
                        getString(R.string.setting_up_slite_error_text, args.sliteName)
                    )
                    completeSetupFlow()
                }
            }
        }

        override fun onLightCharacteristicChanged(address: String, lightCharacteristic: SliteLightCharacteristic) {
            super.onLightCharacteristicChanged(address, lightCharacteristic)
            viewLifecycleOwner.lifecycleScope.launch {
                status = if (lightCharacteristic.blackout == SLITE_STATUS_BLACKOUT) OFF else ON
                lightConfiguration = lightConfiguration.copy(
                    name = args.sliteName,
                    hue = lightCharacteristic.hue,
                    brightness = lightCharacteristic.brightness.roundToInt(),
                    temperature = lightCharacteristic.temperature,
                    saturation = lightCharacteristic.saturation.roundToInt(),
                )
                isLightConfigurationReady = true
                bluetoothService.readSliteModeAndStatus(address)
            }
        }

        override fun onLightConfigurationModeChanged(address: String, lightConfigurationMode: LightConfigurationMode) {
            super.onLightConfigurationModeChanged(address, lightConfigurationMode)
            if (isLightConfigurationReady && !isLightModeReady) {
                isLightModeReady = true
                viewLifecycleOwner.lifecycleScope.launch {
                    analyticsService.trackAddLight()
                    isLoading.value = false

                    lightConfiguration = lightConfiguration.copy(configurationMode = lightConfigurationMode)
                    viewModel.addLight(address, lightConfiguration, status)
                    internalStorageManager.hasUserAddedFirstDevice = true

                    delay(COMPLETE_SETUP_FLOW_DELAY_MS)
                    completeSetupFlow()
                }
            }
        }
    }

    private fun completeSetupFlow() {
        bluetoothService.removeDeviceSubscriber(args.sliteAddress, sliteEventSubscriber)
        navigateUp()
    }

    private fun initDefaultLightConfiguration(): LightConfiguration = LightConfiguration(
        "",
        DEFAULT_LIGHT_HUE,
        DEFAULT_LIGHT_BRIGHTNESS,
        DEFAULT_LIGHT_TEMPERATURE,
        DEFAULT_LIGHT_SATURATION
    )

    companion object {
        private const val COMPLETE_SETUP_FLOW_DELAY_MS = 1000L
    }
}