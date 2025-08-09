package com.sliteptyltd.slite.feature.scenes

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.data.model.extensions.mapToSceneListItems
import com.sliteptyltd.slite.data.model.extensions.toSliteLightCharacteristic
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.scene.Scene
import com.sliteptyltd.slite.databinding.FragmentScenesBinding
import com.sliteptyltd.slite.feature.effectsservice.EffectsHandler
import com.sliteptyltd.slite.feature.effectsservice.EffectsPlayerService.Companion.NO_GROUP_ID
import com.sliteptyltd.slite.feature.scenes.adapter.ScenesAdapter
import com.sliteptyltd.slite.utils.Constants.Connectivity.LIGHT_CONFIGURATION_UPDATE_THROTTLE
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_BLACKOUT
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_ON
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.SliteEventSubscriber
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import com.sliteptyltd.slite.views.statelayout.StateDetails
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.navigation.koinNavGraphViewModel
import kotlin.math.roundToInt

class ScenesFragment : Fragment(R.layout.fragment_scenes) {

    private val binding by viewBinding(FragmentScenesBinding::bind)
    private val viewModel by koinNavGraphViewModel<ScenesViewModel>(R.id.home_graph)
    private val bluetoothService by inject<BluetoothService>()
    private val effectsHandler by inject<EffectsHandler>()
    private val dialogHandler by inject<DialogHandler>()
    private val analyticsService by inject<AnalyticsService>()
    private val scenesAdapter by lazy { initScenesAdapter() }
    private val lightsEventsSubscribers = mutableMapOf<String, SliteEventSubscriber>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSystemBarsOverlaps()
        initViews()
        initObservers()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getScenesList()
        viewModel.getLightsList()
    }

    override fun onPause() {
        viewModel.storeLatestScenesList()
        super.onPause()
    }

    override fun onDestroyView() {
        lightsEventsSubscribers.forEach { (address, sub) ->
            bluetoothService.removeDeviceSubscriber(address, sub)
        }
        lightsEventsSubscribers.clear()
        super.onDestroyView()
    }

    private fun initObservers() {
        viewModel.scenes.observe(viewLifecycleOwner) { scenes ->
            if (scenes.isNullOrEmpty()) {
                binding.stateLayout.setState(scenesEmptyStateDetails)
                scenesAdapter.updateScenesListItems(emptyList())
                return@observe
            }
            binding.stateLayout.setState(StateDetails.Success)
            scenesAdapter.updateScenesListItems(scenes.mapToSceneListItems())

            initSceneLightsSubscribers(scenes)
        }
    }

    private fun initSceneLightsSubscribers(scenes: List<Scene>) {
        val lightsAddresses: List<String> = scenes.map { it.lightsConfigurations.mapNotNull { l -> l.address } }.flatten().distinct()
        lightsAddresses.forEach { sliteAddress ->
            if (sliteAddress !in lightsEventsSubscribers.keys) {
                val sliteEventSubscriber = getSliteEventSubscriber()
                lightsEventsSubscribers[sliteAddress] = sliteEventSubscriber
                bluetoothService.addDeviceSubscriber(sliteAddress, sliteEventSubscriber)
            }
        }
    }

    private fun getSliteEventSubscriber(): SliteEventSubscriber = object : SliteEventSubscriber() {
        override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
            if (!isConnected) {
                effectsHandler.stopEffect(address)
            }
        }

        override fun onLightCharacteristicChanged(address: String, lightCharacteristic: SliteLightCharacteristic) {
            if (lightCharacteristic.blackout == SLITE_STATUS_BLACKOUT) {
                effectsHandler.stopEffect(address)
            }
        }

        override fun onLightConfigurationModeChanged(address: String, lightConfigurationMode: LightConfigurationMode) {
            if (lightConfigurationMode != EFFECTS) {
                effectsHandler.stopEffect(address)
            }
        }
    }

    private fun initListeners() {
        binding.addSceneBtn.setOnClickListener {
            if (viewModel.canCreateScene) {
                dialogHandler.showNameLightDialog(
                    requireContext(),
                    dialogTitle = getString(R.string.scenes_create_dialog_title),
                    confirmButtonText = getString(R.string.scenes_create_dialog_button_text),
                    dialogDescriptionText = getString(R.string.scenes_create_dialog_description),
                    onNameConfirmed = { sceneName ->
                        analyticsService.trackCreateScene()
                        viewModel.createNewScene(sceneName)
                    }
                )
            } else {
                dialogHandler.showSceneCreationRequirementsDialog(requireContext())
            }
        }
    }

    private fun initViews() {
        binding.scenesListRV.adapter = scenesAdapter
    }

    private fun initScenesAdapter(): ScenesAdapter = ScenesAdapter(
        ::onScenePowerButtonClick,
        ::onRenameSceneClick,
        ::onDeleteSceneClick
    )

    private fun onScenePowerButtonClick(sceneId: Int) {
        val scene = viewModel.getSceneById(sceneId) ?: return
        analyticsService.trackApplyScene()
        val groupEffectConfigurations = mutableListOf<Int>()
        scene.lightsConfigurations.forEach { light ->
            val sliteAddress = light.address ?: return@forEach
            effectsHandler.stopEffect(sliteAddress)
            if (!viewModel.isLightConnected(light.lightId, light.groupId)) return@forEach
            viewLifecycleOwner.lifecycleScope.launch {
                delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                if (light.configuration.configurationMode == EFFECTS) {
                    if (light.groupId == NO_GROUP_ID) {
                        bluetoothService.setSliteOutputValue(
                            sliteAddress,
                            light.configuration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                        )
                        delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                        bluetoothService.setSliteConfigurationMode(sliteAddress, light.configuration.configurationMode)
                        delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                        effectsHandler.updateEffectStatus(sliteAddress, light.configuration.effect, true)
                    } else if (light.groupId !in groupEffectConfigurations) {
                        groupEffectConfigurations.add(light.groupId)
                        val lightsAddresses = scene.lightsConfigurations.filter { it.groupId == light.groupId }.mapNotNull { it.address }
                        lightsAddresses.forEach { address ->
                            bluetoothService.setSliteOutputValue(
                                address,
                                light.configuration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                            )
                            delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                            bluetoothService.setSliteConfigurationMode(address, light.configuration.configurationMode)
                            delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                        }
                        effectsHandler.updateEffectStatus(lightsAddresses, light.groupId, light.configuration.effect, true)
                    }
                } else {
                    bluetoothService.setSliteConfigurationMode(sliteAddress, light.configuration.configurationMode)
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        light.configuration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                    )
                }
            }
            viewModel.applySceneLightsDetails(sceneId)
        }
    }

    private fun onRenameSceneClick(sceneId: Int, currentName: String) {
        dialogHandler.showNameLightDialog(
            requireContext(),
            defaultSliteName = currentName,
            dialogTitle = getString(R.string.scenes_rename_dialog_title),
            confirmButtonText = getString(R.string.scenes_rename_dialog_button_text),
            onNameConfirmed = { newSceneName -> viewModel.updateSceneName(sceneId, newSceneName) }
        )
    }

    private fun onDeleteSceneClick(sceneId: Int, sceneName: String) {
        dialogHandler.showLightActionsConfirmationDialog(
            requireContext(),
            getString(R.string.scenes_remove_dialog_title),
            getString(R.string.scenes_remove_dialog_description, sceneName),
            getString(R.string.scenes_remove_confirmation_button_text)
        ) {
            viewModel.removeScene(sceneId)
        }
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.scenesTitleTV) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topInsetsExtra = insets.top + resources.getDimension(R.dimen.home_title_margin_top).roundToInt()

            binding.scenesTitleTV.updateLayoutParams<MarginLayoutParams> {
                topMargin = topInsetsExtra
            }

            binding.addSceneBtn.updateLayoutParams<MarginLayoutParams> {
                topMargin = topInsetsExtra
            }
            CONSUMED
        }
    }

    companion object {
        private val scenesEmptyStateDetails = StateDetails.Error(
            R.drawable.ic_state_empty,
            R.string.scenes_empty_state_title,
            R.string.scenes_empty_state_subtitle
        )
    }
}