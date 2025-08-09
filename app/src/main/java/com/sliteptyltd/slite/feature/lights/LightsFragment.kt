package com.sliteptyltd.slite.feature.lights

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.isInvisible
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.data.model.extensions.extractSliteLightCharacteristic
import com.sliteptyltd.slite.data.model.extensions.getGroupedLightsAddresses
import com.sliteptyltd.slite.data.model.extensions.isDefaultConfiguration
import com.sliteptyltd.slite.data.model.extensions.isSingleLightItem
import com.sliteptyltd.slite.data.model.extensions.toSliteLightCharacteristic
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.data.preference.InternalStorageManager.Companion.NO_NEWLY_CREATED_GROUP_ID
import com.sliteptyltd.slite.databinding.FragmentLightsBinding
import com.sliteptyltd.slite.feature.addcomponent.AddComponentDropdownDialogFragment
import com.sliteptyltd.slite.feature.addcomponent.AddComponentDropdownDialogFragment.AddComponentDropdownActions
import com.sliteptyltd.slite.feature.effectsservice.EffectsHandler
import com.sliteptyltd.slite.feature.lightconfiguration.LightConfigurationDialogFragment.OnConfigurationDialogSwipeActionsClickListener
import com.sliteptyltd.slite.feature.lightconfiguration.LightConfigurationDialogFragment.OnLightConfigurationDoneListener
import com.sliteptyltd.slite.feature.lights.AddLightsTutorialDialogFragment.OnTutorialCompletedCallback
import com.sliteptyltd.slite.feature.lights.adapter.LightsListAdapter
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType
import com.sliteptyltd.slite.utils.AnnouncementHandler
import com.sliteptyltd.slite.utils.Constants.Connectivity.LIGHT_CONFIGURATION_SLIDER_UPDATE_THROTTLE
import com.sliteptyltd.slite.utils.Constants.Connectivity.LIGHT_CONFIGURATION_UPDATE_THROTTLE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_BLACKOUT_STATE
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_ON
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.SliteEventSubscriber
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic.Companion.isDefaultConfiguration
import com.sliteptyltd.slite.utils.extensions.isResumed
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import com.sliteptyltd.slite.views.statelayout.StateDetails
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.navigation.koinNavGraphViewModel
import android.os.Bundle
import kotlin.math.roundToInt

class LightsFragment : Fragment(R.layout.fragment_lights), OnTutorialCompletedCallback, AddComponentDropdownActions,
    OnConfigurationDialogSwipeActionsClickListener, OnLightConfigurationDoneListener {

    private val binding by viewBinding(FragmentLightsBinding::bind)
    private val viewModel by koinNavGraphViewModel<LightsViewModel>(R.id.home_graph)
    private val internalStorageManager by inject<InternalStorageManager>()
    private val dialogHandler by inject<DialogHandler>()
    private val announcementHandler by inject<AnnouncementHandler>()
    private val effectsHandler by inject<EffectsHandler>()
    private val lightsListAdapter: LightsListAdapter by lazy { initLightsListAdapter() }
    private val bluetoothService by inject<BluetoothService>()
    private val analyticsService by inject<AnalyticsService>()
    private val lightsEventsSubscribers = mutableMapOf<String, SliteEventSubscriber>()
    private val configurationChangeJobs = mutableMapOf<String, Job>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getLightsList()
        initViews()
        initListeners()
        initObservers()
        handleSystemBarsOverlaps()
    }

    override fun onPause() {
        viewModel.storeLatestLights()
        super.onPause()
    }

    override fun onDestroyView() {
        viewModel.shouldUpdateIndividualConnections = true
        viewModel.shouldUpdateGroupsConnections = true
        lightsEventsSubscribers.forEach { (address, sub) ->
            bluetoothService.removeDeviceSubscriber(address, sub)
        }
        lightsEventsSubscribers.clear()
        super.onDestroyView()
    }

    override fun onLightConfigurationDone() {
        viewModel.isLightDetailsConfigurationOngoing = false
        viewModel.getLightsList()
    }

    override fun onLightConfigurationResumed() {
        viewModel.isLightDetailsConfigurationOngoing = true
    }

    private fun initViews() {
        binding.lightsListRV.adapter = lightsListAdapter
    }

    private fun initListeners() {
        binding.addSliteBtn.setOnClickListener {
            showAddComponentDropdown()
        }

        binding.reconnectLightsBtn.setOnClickListener {
            reconnectLights()
        }

        binding.moreBtn.setOnClickListener {
            dialogHandler.showSupportDialog(
                context = requireContext(),
                areUpdatesAvailable = bluetoothService.isUpdateAvailableForAtLeastOneDevice,
                onEmailClientNotFound = {
                    announcementHandler.showWarningAnnouncement(
                        requireActivity(),
                        getString(R.string.support_dialog_email_client_not_found_description)
                    )
                },
                onOpenUpdateScreen = {
                    findNavController().navigate(LightsFragmentDirections.actionLightFragmentToUpdateLightsFragment())
                }
            )
        }
    }

    private fun reconnectLights() {
        val disconnectedLightsAddresses = viewModel.disconnectedLightsAddresses.value ?: return
        binding.reconnectLightsBtn.isInvisible = true
        binding.reconnectLoadingIndicator.isInvisible = false
        disconnectedLightsAddresses.forEach { lightAddress ->
            addSliteEventSubscriberAndReadInfo(lightAddress, getReconnectSliteEventSubscriber())
        }
        connectToIndividualLights(disconnectedLightsAddresses)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(RECONNECT_PROGRESS_INDICATOR_MAX_VISIBILITY_DURATION_MS)
            if (viewModel.disconnectedLightsAddresses.value?.isNotEmpty() == true) {
                binding.reconnectLightsBtn.isInvisible = false
                binding.reconnectLoadingIndicator.isInvisible = true
            }
        }
    }

    private fun getReconnectSliteEventSubscriber(): SliteEventSubscriber = object : SliteEventSubscriber() {
        override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
            lightsEventsSubscribers.remove(address)
            bluetoothService.removeDeviceSubscriber(address, this)
            if (isConnected) {
                addSliteEventSubscriberAndReadInfo(address, getSliteEventSubscriber())
                bluetoothService.readSliteOutputValue(address)
            }
        }
    }

    private fun initObservers() {
        viewModel.lightsListItems.observe(viewLifecycleOwner) { displayedLights ->
            if (displayedLights.isNullOrEmpty()) {
                binding.stateLayout.setState(lightsEmptyStateDetails)
                lightsListAdapter.updateLightsListItems(emptyList())
                return@observe
            }

            if (viewModel.shouldUpdateListUI) {
                binding.stateLayout.setState(StateDetails.Success)
                lightsListAdapter.updateLightsListItems(displayedLights)
            }
        }

        viewModel.individualLights.observe(viewLifecycleOwner) { individualLights ->
            individualLights ?: return@observe
            if (individualLights.isNotEmpty() && viewModel.shouldUpdateIndividualConnections) {
                val individualLightsAddresses = individualLights.mapNotNull { it.address }
                setLightsEventsSubscribers(individualLightsAddresses)
                connectToIndividualLights(individualLightsAddresses)
                if (viewModel.shouldReadLightsInfoAfterUngroup) {
                    viewModel.shouldReadLightsInfoAfterUngroup = false
                    individualLightsAddresses.forEach { address -> bluetoothService.readSliteOutputValue(address) }
                }
                viewModel.shouldUpdateIndividualConnections = false
            }
        }

        viewModel.lightsGroups.observe(viewLifecycleOwner) { lightsGroups ->
            lightsGroups ?: return@observe
            if (lightsGroups.isNotEmpty() && viewModel.shouldUpdateGroupsConnections) {
                lightsGroups.forEach { lightsGroup ->
                    val groupedLightsAddresses = lightsGroup.lightConfiguration.lightsDetails.mapNotNull { it.address }
                    if (lightsGroup.id == internalStorageManager.newlyCreatedGroupId) {
                        internalStorageManager.newlyCreatedGroupId = NO_NEWLY_CREATED_GROUP_ID
                        viewLifecycleOwner.lifecycleScope.launch {
                            groupedLightsAddresses.forEach { sliteAddress ->
                                effectsHandler.stopEffect(sliteAddress)
                                delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                                bluetoothService.setSliteConfigurationMode(sliteAddress, lightsGroup.lightConfiguration.configurationMode)
                                bluetoothService.setSliteOutputValue(sliteAddress, lightsGroup.extractSliteLightCharacteristic())
                            }
                        }.invokeOnCompletion {
                            setLightsEventsSubscribers(groupedLightsAddresses)
                        }
                    } else {
                        setLightsEventsSubscribers(groupedLightsAddresses)
                        connectToIndividualLights(groupedLightsAddresses)
                    }
                }
                viewModel.shouldUpdateGroupsConnections = false
            }
        }

        viewModel.disconnectedLightsAddresses.observe(viewLifecycleOwner) { disconnectedLightsAddresses ->
            disconnectedLightsAddresses ?: return@observe
            binding.reconnectLoadingIndicator.isInvisible = true
            binding.reconnectLightsBtn.isInvisible = disconnectedLightsAddresses.isEmpty()
        }
    }

    private fun setLightsEventsSubscribers(lightsAddresses: List<String?>) {
        lightsAddresses.forEach { lightAddress ->
            lightAddress ?: return@forEach
            addSliteEventSubscriberAndReadInfo(lightAddress, getSliteEventSubscriber())
        }
    }

    private fun addSliteEventSubscriberAndReadInfo(sliteAddress: String, sliteEventSubscriber: SliteEventSubscriber) {
        if (sliteAddress !in lightsEventsSubscribers.keys) {
            lightsEventsSubscribers[sliteAddress] = sliteEventSubscriber
            bluetoothService.addDeviceSubscriber(sliteAddress, sliteEventSubscriber)
        }
    }

    private fun connectToIndividualLights(lightsAddresses: List<String>) {
        lightsAddresses.forEach { lightAddress ->
            bluetoothService.connect(lightAddress)
        }
    }

    private fun getSliteEventSubscriber(): SliteEventSubscriber = object : SliteEventSubscriber() {

        override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
            if (viewModel.isLightDetailsConfigurationOngoing) return

            val lightId = address.hashCode()
            val status = if (isConnected) ON else DISCONNECTED

            val light = viewModel.getLightById(lightId)
            val isGroupedLight = light == null
            if (isGroupedLight) {
                val individualMode = viewModel.getGroupedLightIndividualModeById(lightId)
                val newMode: LightConfigurationMode?
                if (!isConnected && individualMode == EFFECTS) {
                    newMode = LightConfigurationMode.COLORS
                    effectsHandler.stopEffect(address)
                } else {
                    newMode = null
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.updateLightsGroupUI(lightId, status, newMode)
                }
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.changeLightStatus(lightId, status)
                    if (!isConnected && light?.lightConfiguration?.configurationMode == EFFECTS) {
                        effectsHandler.stopEffect(address)
                    }
                }
            }

            if (!isConnected) {
                lightsEventsSubscribers[address]?.let {
                    bluetoothService.removeDeviceSubscriber(address, it)
                }
                lightsEventsSubscribers.remove(address)
            }
        }

        override fun onLightCharacteristicChanged(address: String, lightCharacteristic: SliteLightCharacteristic) {
            if (viewModel.isLightDetailsConfigurationOngoing) return

            val lightId = address.hashCode()
            val currentLight = viewModel.getLightById(lightId)
            val lightConfiguration = currentLight?.lightConfiguration ?: viewModel.getGroupedLightConfigurationById(lightId) ?: return

            val status = if (lightCharacteristic.blackout == SLITE_STATUS_ON) ON else OFF
            val isGroupedLight = currentLight == null
            if (isGroupedLight) {
                handleGroupedLightCharacteristicChanged(
                    lightId,
                    address,
                    status,
                    lightConfiguration,
                    isLightReconnected = !lightConfiguration.isDefaultConfiguration() && lightCharacteristic.isDefaultConfiguration(),
                )
            } else {
                handleSingleLightCharacteristicChange(lightId, status, lightConfiguration, lightCharacteristic)
            }
        }

        override fun onLightConfigurationModeChanged(address: String, lightConfigurationMode: LightConfigurationMode) {
            if (viewModel.isLightDetailsConfigurationOngoing) return

            val lightId = address.hashCode()
            val currentLight = viewModel.getLightById(lightId)
            val lightConfiguration = currentLight?.lightConfiguration ?: viewModel.getGroupedLightConfigurationById(lightId) ?: return
            val individualMode = viewModel.getGroupedLightIndividualModeById(lightId)
            val individualStatus = viewModel.getGroupedLightIndividualStatusById(lightId)
            val isIndividualLight = currentLight != null

            if (lightConfigurationMode != EFFECTS && lightConfiguration.configurationMode == EFFECTS) {
                if (individualMode == null) {
                    effectsHandler.stopEffect(address)
                } else if (individualMode != lightConfigurationMode) {
                    val groupId = viewModel.getGroupedLightGroupId(lightId) ?: return
                    effectsHandler.updateEffectStatus(listOf(address), groupId, null, (individualStatus == ON))
                }
            }
            if (isIndividualLight && currentLight != null && lightConfigurationMode != lightConfiguration.configurationMode) {
                handleSingleLightCharacteristicChange(
                    lightId,
                    currentLight.status,
                    lightConfiguration.copy(configurationMode = lightConfigurationMode)
                )
            } else if (!isIndividualLight && individualMode != lightConfigurationMode && individualStatus != null) {
                handleGroupedLightCharacteristicChanged(
                    lightId,
                    address,
                    individualStatus,
                    lightConfiguration.copy(configurationMode = lightConfigurationMode),
                    false
                )
            }
        }
    }

    private fun handleGroupedLightCharacteristicChanged(
        lightId: Int,
        address: String,
        status: LightStatus,
        lightConfiguration: LightConfiguration,
        isLightReconnected: Boolean,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateLightsGroupUI(lightId, status, lightConfiguration.configurationMode)
        }

//        if (lightConfiguration.configurationMode == EFFECTS && status == ON) {
//            val groupedLights = lightConfiguration.lightsDetails
//                .filter { it.status == ON && it.address != null }
//                .mapNotNull { it.address }.toMutableList()
//            groupedLights.add(address)
//            bluetoothService.setSliteConfigurationMode(address, EFFECTS)
//            effectsHandler.updateEffectStatus(groupedLights, lightConfiguration.effect, true)
//        } else
        if (isLightReconnected) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                bluetoothService.setSliteConfigurationMode(address, lightConfiguration.configurationMode)
                bluetoothService.setSliteOutputValue(
                    address,
                    lightConfiguration.toSliteLightCharacteristic(DEFAULT_LIGHT_BLACKOUT_STATE)
                )
            }
        }
    }

    private fun handleSingleLightCharacteristicChange(
        lightId: Int,
        status: LightStatus,
        lightConfiguration: LightConfiguration,
        lightCharacteristic: SliteLightCharacteristic? = null
    ) {
        val updatedConfiguration = if (lightCharacteristic != null) {
            lightConfiguration.copy(
                hue = lightCharacteristic.hue,
                brightness = lightCharacteristic.brightness.roundToInt(),
                temperature = lightCharacteristic.temperature,
                saturation = lightCharacteristic.saturation.roundToInt()
            )
        } else {
            lightConfiguration
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateLightUI(lightId, updatedConfiguration, status)
            viewModel.getLightsList()
        }

//        if (status == ON && lightConfiguration.configurationMode == EFFECTS && lightCharacteristic != null) {
//            bluetoothService.setSliteConfigurationMode(address, EFFECTS)
//            effectsHandler.updateEffectStatus(address, lightConfiguration.effect, true)
//        }
    }

    private fun initLightsListAdapter(): LightsListAdapter =
        LightsListAdapter(
            ::onSectionHeaderDropdownButtonClick,
            ::onSectionControlActionClick,
            ::onRenameLightClick,
            ::onDeleteLightClick,
            ::onLightPowerButtonClick,
            ::onLightConfigurationButtonClick,
            ::onLightConfigurationUpdated,
            ::trackLightConfigurationEvent
        )

    private fun onLightConfigurationUpdated(lightId: Int, lightConfiguration: LightConfiguration) {
        viewModel.updateLightConfiguration(lightId, lightConfiguration)

        if (lightConfiguration.isIndividualLight) {
            val sliteAddress = viewModel.getLightById(lightId)?.address ?: return
            throttleLatestSliteChange(LIGHT_CONFIGURATION_SLIDER_UPDATE_THROTTLE, sliteAddress, viewLifecycleOwner.lifecycleScope) {
                if (!viewLifecycleOwner.lifecycle.isResumed) return@throttleLatestSliteChange
                val light = viewModel.getLightById(lightId) ?: return@throttleLatestSliteChange
                bluetoothService.setSliteOutputValue(
                    sliteAddress,
                    light.extractSliteLightCharacteristic()
                )
            }
        } else {
            throttleLatestSliteChange(LIGHT_CONFIGURATION_SLIDER_UPDATE_THROTTLE, lightId.toString(), viewLifecycleOwner.lifecycleScope) {
                if (!viewLifecycleOwner.lifecycle.isResumed) return@throttleLatestSliteChange
                val lightsGroup = viewModel.getLightById(lightId) ?: return@throttleLatestSliteChange
                lightsGroup.lightConfiguration.lightsDetails.forEach { light ->
                    val sliteAddress = light.address ?: return@forEach
                    bluetoothService.setSliteConfigurationMode(sliteAddress, lightConfiguration.configurationMode)
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        lightsGroup.extractSliteLightCharacteristic()
                    )
                }
            }
        }
    }

    private fun onLightPowerButtonClick(lightId: Int, currentStatus: LightStatus) {
        when (currentStatus) {
            ON -> changeLightStatus(lightId, OFF)
            OFF -> changeLightStatus(lightId, ON)
            DISCONNECTED -> dialogHandler.showDisconnectedLightInfoDialog(
                requireContext(),
                ::reconnectLights
            )
        }
    }

    private fun changeLightStatus(lightId: Int, currentStatus: LightStatus) {
        viewModel.changeLightStatus(lightId, currentStatus)
        val light = viewModel.getLightById(lightId) ?: return
        val isPoweredOn = currentStatus == ON

        if (light.isSingleLightItem()) {
            analyticsService.trackLightBlackoutStateChanged(!isPoweredOn)
            val sliteAddress = light.address ?: return
            bluetoothService.setSliteConfigurationMode(sliteAddress, light.lightConfiguration.configurationMode)
            if (light.lightConfiguration.configurationMode == EFFECTS) {
                effectsHandler.updateEffectStatus(sliteAddress, light.lightConfiguration.effect, isPoweredOn)
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        light.extractSliteLightCharacteristic()
                    )
                }
            } else {
                bluetoothService.setSliteOutputValue(
                    sliteAddress,
                    light.extractSliteLightCharacteristic()
                )
            }
        } else {
            analyticsService.trackGroupBlackoutStateChanged(!isPoweredOn)
            light.lightConfiguration.lightsDetails.forEach {
                val sliteAddress = it.address ?: return@forEach
                bluetoothService.setSliteConfigurationMode(
                    sliteAddress,
                    light.lightConfiguration.configurationMode
                )
            }
            if (light.lightConfiguration.configurationMode == EFFECTS) {
                effectsHandler.updateEffectStatus(
                    light.lightConfiguration.getGroupedLightsAddresses(),
                    light.id,
                    light.lightConfiguration.effect,
                    isPoweredOn
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                    light.lightConfiguration.lightsDetails.forEach {
                        val sliteAddress = it.address ?: return@forEach
                        bluetoothService.setSliteOutputValue(
                            sliteAddress,
                            light.extractSliteLightCharacteristic()
                        )
                    }
                }
            } else {
                light.lightConfiguration.lightsDetails.forEach {
                    val sliteAddress = it.address ?: return@forEach
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        light.extractSliteLightCharacteristic()
                    )
                }
            }
        }
    }

    private fun onSectionHeaderDropdownButtonClick(sectionType: LightsSectionType, newDropdownState: Boolean) {
        viewModel.changeSectionDropdownState(sectionType, newDropdownState)
    }

    private fun onSectionControlActionClick(sectionType: LightsSectionType, isSectionStateOn: Boolean) {
        viewModel.changeSectionPowerState(sectionType, isSectionStateOn)
    }

    private fun onRenameLightClick(lightId: Int, isLightsGroup: Boolean, currentLightName: String) {
        val dialogTitle = if (isLightsGroup) {
            getString(R.string.create_group_name_dialog_title)
        } else {
            getString(R.string.name_slite_title)
        }
        dialogHandler.showNameLightDialog(
            requireContext(),
            currentLightName,
            dialogTitle,
            getString(R.string.name_slite_confirm_button_text),
            onNameConfirmed = { sliteName -> viewModel.renameLight(lightId, isLightsGroup, sliteName) }
        )
    }

    private fun onDeleteLightClick(lightId: Int, lightName: String, isLightsGroup: Boolean) {
        val dialogTitle: String
        val dialogDescription: String
        val confirmButtonText: String
        val containingScenesList = viewModel.getContainingScenesList(lightId)
        val containingScenesDeletionWarning = if (containingScenesList.isNotEmpty()) {
            getString(R.string.delete_light_dialog_containing_scenes_warning)
        } else {
            ""
        }
        if (isLightsGroup) {
            dialogTitle = getString(R.string.delete_light_dialog_group_title)
            dialogDescription =
                getString(R.string.delete_light_dialog_description_group_format, lightName) + containingScenesDeletionWarning
            confirmButtonText = getString(R.string.delete_light_group_confirm_button_text)
        } else {
            dialogTitle = getString(R.string.delete_light_dialog_individual_light_title)
            dialogDescription =
                getString(R.string.delete_light_dialog_description_individual_light_format, lightName) + containingScenesDeletionWarning
            confirmButtonText = getString(R.string.delete_light_individual_confirm_button_text)
        }

        dialogHandler.showLightActionsConfirmationDialog(
            requireContext(),
            dialogTitle,
            dialogDescription,
            confirmButtonText,
            containingScenesList
        ) {
            if (isLightsGroup) {
                viewModel.shouldReadLightsInfoAfterUngroup = true
                viewModel.disbandLightsGroup(lightId)
            } else {
                val sliteAddress = viewModel.getLightById(lightId)?.address
                if (sliteAddress != null) {
                    effectsHandler.stopEffect(sliteAddress)
                    bluetoothService.disconnect(sliteAddress)
                }
                viewModel.deleteLight(lightId)
            }
        }
    }

    private fun onLightConfigurationButtonClick(lightId: Int) {
        trackLightConfigurationEvent(lightId)
        viewModel.isLightDetailsConfigurationOngoing = true
        findNavController().navigate(LightsFragmentDirections.actionLightsFragmentToLightConfigurationDialogFragment(lightId))
    }

    private fun trackLightConfigurationEvent(lightId: Int) {
        val lightConfiguration = viewModel.getLightById(lightId)?.lightConfiguration ?: return
        analyticsService.trackAdvancedModeInteractions(lightConfiguration)
    }

    private fun showAddLightsTutorialDialogFragment(rightMargin: Int, topMargin: Int) {
        if (!internalStorageManager.hasUserSeenAddLightsTutorial) {
            AddLightsTutorialDialogFragment.show(childFragmentManager, rightMargin, topMargin)
            internalStorageManager.hasUserSeenAddLightsTutorial = true
        }
    }

    override fun completeAddLightsTutorial(shouldProceedToAddLightsFlow: Boolean) {
        if (shouldProceedToAddLightsFlow) {
            showAddComponentDropdown()
        }
    }

    private fun showAddComponentDropdown() {
        AddComponentDropdownDialogFragment.show(childFragmentManager, viewModel.canCreateNewGroup, viewModel.canCreateNewScene)
    }

    override fun onAddLightClick() {
        findNavController().navigate(LightsFragmentDirections.actionLightsFragmentToAddLightFragment())
    }

    override fun onNewGroupClick() {
        findNavController().navigate(LightsFragmentDirections.actionLightsFragmentToCreateGroupFragment())
    }

    override fun onSaveSceneClick() {
        dialogHandler.showNameLightDialog(
            requireContext(),
            dialogTitle = getString(R.string.scenes_create_dialog_title),
            confirmButtonText = getString(R.string.scenes_create_dialog_button_text),
            dialogDescriptionText = getString(R.string.scenes_create_dialog_description),
            onNameConfirmed = { sceneName ->
                analyticsService.trackCreateScene()
                viewModel.createNewScene(sceneName)
                announcementHandler.showSuccessAnnouncement(requireActivity(), getString(R.string.scenes_creation_success_message))
            }
        )
    }

    override fun onConfigurationDialogRenameLightClick(lightId: Int, isLightsGroup: Boolean, currentLightName: String) {
        onRenameLightClick(lightId, isLightsGroup, currentLightName)
    }

    override fun onConfigurationDialogDeleteLightClick(lightId: Int, lightName: String, isLightsGroup: Boolean) {
        onDeleteLightClick(lightId, lightName, isLightsGroup)
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.lightsTitleTV) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topInsetsExtra = insets.top + resources.getDimension(R.dimen.home_title_margin_top).roundToInt()

            binding.lightsTitleTV.updateLayoutParams<MarginLayoutParams> {
                topMargin = topInsetsExtra
            }
            binding.moreBtn.updateLayoutParams<MarginLayoutParams> {
                topMargin = topInsetsExtra
            }
            binding.addSliteBtn.updateLayoutParams<MarginLayoutParams> {
                topMargin = topInsetsExtra
            }
            binding.reconnectLightsBtn.updateLayoutParams<MarginLayoutParams> {
                topMargin = topInsetsExtra
            }
            showAddLightsTutorialDialogFragment(
                binding.addSliteBtn.marginRight + binding.moreBtn.layoutParams.width + binding.moreBtn.marginRight,
                binding.addSliteBtn.marginTop
            )
            CONSUMED
        }
    }

    private fun throttleLatestSliteChange(
        throttleDelayMs: Long,
        sliteAddress: String,
        coroutineScope: CoroutineScope,
        throttleAction: () -> Unit
    ) {
        if (configurationChangeJobs[sliteAddress]?.isActive == true) return
        configurationChangeJobs[sliteAddress] = coroutineScope.launch {
            delay(throttleDelayMs)
            throttleAction()
        }.apply { invokeOnCompletion { configurationChangeJobs.remove(sliteAddress) } }
    }

    companion object {
        private const val RECONNECT_PROGRESS_INDICATOR_MAX_VISIBILITY_DURATION_MS = 6000L
        private val lightsEmptyStateDetails = StateDetails.Error(
            R.drawable.ic_state_empty,
            R.string.lights_empty_state_title,
            R.string.lights_empty_state_subtitle
        )
    }
}