package com.sliteptyltd.slite.feature.lightconfiguration

import android.Manifest.permission.POST_NOTIFICATIONS
import android.animation.LayoutTransition
import android.animation.LayoutTransition.CHANGING
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sliteptyltd.slite.FragmentLightConfigurationBinding
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.data.model.extensions.groupDisconnectedLightsCount
import com.sliteptyltd.slite.data.model.extensions.groupOffLightsCount
import com.sliteptyltd.slite.data.model.extensions.toSliteLightCharacteristic
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.feature.effectsservice.EffectsHandler
import com.sliteptyltd.slite.feature.lightconfiguration.LightConfigurationViewModel.Companion.copyConfigurationWithUpdatedValues
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationAdapter
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem
import com.sliteptyltd.slite.utils.Constants.Connectivity.LIGHT_CONFIGURATION_SLIDER_UPDATE_THROTTLE
import com.sliteptyltd.slite.utils.Constants.Connectivity.LIGHT_CONFIGURATION_UPDATE_THROTTLE
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_BLACKOUT
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_ON
import com.sliteptyltd.slite.utils.autoCleared
import com.sliteptyltd.slite.utils.bindingadapters.setLightName
import com.sliteptyltd.slite.utils.bindingadapters.setLightPowerButtonImage
import com.sliteptyltd.slite.utils.bindingadapters.setLightStatusText
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.SliteEventSubscriber
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic
import com.sliteptyltd.slite.utils.color.colorSaturation
import com.sliteptyltd.slite.utils.color.colorWithMaxBrightness
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class LightConfigurationDialogFragment : DialogFragment() {

    private var binding by autoCleared<FragmentLightConfigurationBinding>()
    private val viewModel by viewModel<LightConfigurationViewModel> { parametersOf(args.lightId) }
    private val internalStorageManager by inject<InternalStorageManager>()
    private val args by navArgs<LightConfigurationDialogFragmentArgs>()
    private val dialogHandler by inject<DialogHandler>()
    private val effectsHandler by inject<EffectsHandler>()
    private val bluetoothService by inject<BluetoothService>()
    private val analyticsService by inject<AnalyticsService>()
    private val lightsEventsSubscribers = mutableMapOf<String, SliteEventSubscriber>()
    private val lightConfigurationAdapter by lazy { initLightConfigurationAdapter() }
    private var configurationChangeJob: Job? = null
    private val requestNotificationsPermissionLauncher = initRequestNotificationsPermissionLauncher()
    private val hasNotificationsPermission: Boolean @RequiresApi(TIRAMISU) get() = getHasNotificationsPermissions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedBottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lightConfigurationDoneListener?.onLightConfigurationResumed()

        return super.onCreateDialog(savedInstanceState).apply {
            val dialogWindow = window ?: return@apply
            WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLightConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSystemBarsOverlaps()
        initConfigurationOptions()
        initListeners()
        setupViews()

        viewModel.selectedLightDetails.observe(viewLifecycleOwner) {
            val lightConfigurationDetails = it ?: return@observe
            setupLightDetailsCard()
            val disconnectedCount = lightConfigurationDetails.configuration.groupDisconnectedLightsCount
            val offCount = lightConfigurationDetails.configuration.groupOffLightsCount
            val isLightsGroup = lightConfigurationDetails.configuration.isLightsGroup
            if (isLightsGroup && disconnectedCount + offCount == lightConfigurationDetails.configuration.lightsDetails.size) {
                dismiss()
                return@observe
            }

            if (viewModel.shouldConnect) {
                initLightsEventsSubscribers(lightConfigurationDetails)
                lightsEventsSubscribers.forEach { (sliteAddress, sliteEventSubscriber) ->
                    bluetoothService.addDeviceSubscriber(sliteAddress, sliteEventSubscriber)
                }
                viewModel.shouldConnect = false
            }
        }
    }

    override fun onDestroyView() {
        lightsEventsSubscribers.forEach { (sliteAddress, sliteEventSubscriber) ->
            bluetoothService.removeDeviceSubscriber(sliteAddress, sliteEventSubscriber)
        }
        super.onDestroyView()
    }

    private fun initLightsEventsSubscribers(lightConfigurationDetails: LightConfigurationDetails) {
        if (lightConfigurationDetails.configuration.isIndividualLight) {
            val sliteAddress = lightConfigurationDetails.address ?: return
            lightsEventsSubscribers[sliteAddress] = initSliteEventSubscriber()
        } else {
            lightConfigurationDetails.configuration.lightsDetails.forEach { _ ->
                initLightsGroupSubscribers().forEach subscribers@{ (address, sliteEventSubscriber) ->
                    lightsEventsSubscribers[address] = sliteEventSubscriber
                }
            }
        }
    }

    private fun initLightsGroupSubscribers(): Map<String, SliteEventSubscriber> {
        val subscribers = mutableMapOf<String, SliteEventSubscriber>()
        val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return emptyMap()
        lightConfiguration.lightsDetails.forEach { light ->
            light.address ?: return@forEach
            subscribers[light.address] = object : SliteEventSubscriber() {
                override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
                    val status = if (isConnected) ON else DISCONNECTED
                    val individualMode =
                        viewModel.selectedLightDetails.value?.configuration?.lightsDetails?.firstOrNull { it.lightId == address.hashCode() }?.mode
                            ?: return
                    val newMode: LightConfigurationMode?
                    if (!isConnected && individualMode == EFFECTS) {
                        newMode = COLORS
                        effectsHandler.stopEffect(address)
                    } else {
                        newMode = null
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.updateLightsGroupUI(address.hashCode(), status, newMode)
                    }
                }

                override fun onLightCharacteristicChanged(address: String, lightCharacteristic: SliteLightCharacteristic) {
                    val status = if (lightCharacteristic.blackout == SLITE_STATUS_ON) ON else OFF
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.updateLightsGroupUI(address.hashCode(), status, null)
                    }
                }

                override fun onLightConfigurationModeChanged(address: String, lightConfigurationMode: LightConfigurationMode) {
                    val configuration = viewModel.selectedLightDetails.value?.configuration ?: return
                    val lightId = address.hashCode()
                    val individualLightMode = configuration.lightsDetails.firstOrNull { it.lightId == lightId }?.mode ?: return
                    if (lightConfigurationMode != EFFECTS && configuration.configurationMode == EFFECTS && individualLightMode != lightConfigurationMode) {
                        effectsHandler.stopEffect(address)
                    }
                    if (lightConfigurationMode != individualLightMode) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.updateLightsGroupUI(lightId, null, lightConfigurationMode)
                        }
                    }
                }
            }
        }
        return subscribers
    }

    private fun initSliteEventSubscriber(): SliteEventSubscriber = object : SliteEventSubscriber() {

        override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
            if (!isConnected) {
                if (viewModel.selectedLightDetails.value?.configuration?.configurationMode == EFFECTS) {
                    effectsHandler.stopEffect(address)
                }
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    viewModel.setLightDisconnectedStatus()
                    dismiss()
                }
            }
        }

        override fun onLightCharacteristicChanged(address: String, lightCharacteristic: SliteLightCharacteristic) {
            if (lightCharacteristic.blackout == SLITE_STATUS_BLACKOUT) {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    viewModel.powerOffLight()
                    dismiss()
                }
            } else {
                val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return
                val updatedConfiguration = lightConfiguration.copy(
                    hue = lightCharacteristic.hue,
                    brightness = lightCharacteristic.brightness.roundToInt(),
                    temperature = lightCharacteristic.temperature,
                    saturation = lightCharacteristic.saturation.roundToInt()
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.updateLightConfiguration(updatedConfiguration)
                    lightConfigurationAdapter.submitList(getConfigurationOptionsList(updatedConfiguration.configurationMode))
                }
            }
        }

        override fun onLightConfigurationModeChanged(address: String, lightConfigurationMode: LightConfigurationMode) {
            val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return
            if (lightConfigurationMode != EFFECTS && lightConfiguration.configurationMode == EFFECTS) {
                effectsHandler.stopEffect(address)
            }
            if (lightConfiguration.isIndividualLight && lightConfiguration.configurationMode != lightConfigurationMode) {
                binding.configurationNavigationBar.selectConfigurationTab(lightConfigurationMode)
                handleConfigurationModeChanged(lightConfigurationMode, true)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.root.setOnClickListener {
            dismiss()
        }

        binding.dialogDragableIndicatorIV.setOnTouchListener { _, _ ->
            dismiss()
            return@setOnTouchListener true
        }

        binding.openActionsIB.setOnClickListener {
            binding.swipeLayout.open()
        }

        binding.editSliteNameBtn.root.setOnClickListener {
            val lightConfigurationDetails = viewModel.selectedLightDetails.value ?: return@setOnClickListener

            dismiss()
            swipeActionsListener?.onConfigurationDialogRenameLightClick(
                lightConfigurationDetails.id,
                lightConfigurationDetails.configuration.isLightsGroup,
                lightConfigurationDetails.configuration.name
            )
        }

        binding.deleteSliteBtn.root.setOnClickListener {
            val lightConfigurationDetails = viewModel.selectedLightDetails.value ?: return@setOnClickListener

            dismiss()
            swipeActionsListener?.onConfigurationDialogDeleteLightClick(
                lightConfigurationDetails.id,
                lightConfigurationDetails.configuration.name,
                lightConfigurationDetails.configuration.isLightsGroup
            )
        }

        binding.lightPowerIV.setOnClickListener {
            viewModel.powerOffLight()
            if (viewModel.selectedLightDetails.value?.configuration?.configurationMode == EFFECTS) {
                handleEffectChanges()
                Handler(Looper.getMainLooper()).postDelayed({
                    val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return@postDelayed
                    if (lightConfiguration.isIndividualLight) {
                        val sliteAddress = viewModel.selectedLightDetails.value?.address ?: return@postDelayed
                        analyticsService.trackLightBlackoutStateChanged(true)
                        bluetoothService.setSliteOutputValue(
                            sliteAddress,
                            lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_BLACKOUT)
                        )
                        dismiss()
                    } else {
                        analyticsService.trackGroupBlackoutStateChanged(true)
                        lightConfiguration.lightsDetails.forEach { light ->
                            val sliteAddress = light.address ?: return@forEach
                            bluetoothService.setSliteOutputValue(
                                sliteAddress,
                                lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_BLACKOUT)
                            )
                        }
                        dismiss()
                    }
                }, LIGHT_CONFIGURATION_UPDATE_THROTTLE)
            } else {
                val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return@setOnClickListener
                if (lightConfiguration.isIndividualLight) {
                    val sliteAddress = viewModel.selectedLightDetails.value?.address ?: return@setOnClickListener
                    analyticsService.trackLightBlackoutStateChanged(true)
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_BLACKOUT)
                    )
                    dismiss()
                } else {
                    analyticsService.trackGroupBlackoutStateChanged(true)
                    lightConfiguration.lightsDetails.forEach { light ->
                        val sliteAddress = light.address ?: return@forEach
                        bluetoothService.setSliteOutputValue(
                            sliteAddress,
                            lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_BLACKOUT)
                        )
                    }
                    dismiss()
                }
            }
        }
    }

    private fun initConfigurationOptions() {
        binding.lightConfigurationOptionsRV.adapter = lightConfigurationAdapter
        val configurationMode = viewModel.selectedLightDetails.value?.configuration?.configurationMode ?: return

        lightConfigurationAdapter.submitList(getConfigurationOptionsList(configurationMode))
    }

    private fun onLightConfigurationUpdated(lightConfigurationItem: LightConfigurationItem) {
        viewModel.updateLightConfiguration(lightConfigurationItem)

        if (lightConfigurationItem is LightConfigurationItem.EffectsConfigurationItem) {
            if (SDK_INT >= TIRAMISU) {
                handleNotificationPermissions()
            } else {
                handleEffectChanges()
            }
            return
        }

        if (viewModel.selectedLightDetails.value?.configuration?.isIndividualLight == true) {
            throttleLatestSliteChange(LIGHT_CONFIGURATION_SLIDER_UPDATE_THROTTLE, viewLifecycleOwner.lifecycleScope) {
                val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return@throttleLatestSliteChange
                val sliteAddress = viewModel.selectedLightDetails.value?.address ?: return@throttleLatestSliteChange
                bluetoothService.setSliteOutputValue(
                    sliteAddress,
                    lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                )
            }
        } else {
            throttleLatestSliteChange(LIGHT_CONFIGURATION_SLIDER_UPDATE_THROTTLE, viewLifecycleOwner.lifecycleScope) {
                val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return@throttleLatestSliteChange
                lightConfiguration.lightsDetails.forEach {
                    val sliteAddress = it.address ?: return@forEach
                    bluetoothService.setSliteConfigurationMode(sliteAddress, lightConfiguration.configurationMode)
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                    )
                }
            }
        }
    }

    private fun throttleLatestSliteChange(
        throttleDelayMs: Long,
        coroutineScope: CoroutineScope,
        throttleAction: () -> Unit
    ) {
        if (configurationChangeJob?.isActive == true) return
        configurationChangeJob = coroutineScope.launch {
            delay(throttleDelayMs)
            throttleAction()
        }.apply { invokeOnCompletion { configurationChangeJob = null } }
    }

    private fun getConfigurationOptionsList(configurationMode: LightConfigurationMode): List<LightConfigurationItem> {
        val lightConfigurationDetails = viewModel.selectedLightDetails.value ?: return emptyList()

        val configurationOptionsList = mutableListOf<LightConfigurationItem>()

        if (configurationMode == EFFECTS) {
            configurationOptionsList.add(LightConfigurationItem.EffectsConfigurationItem(lightConfigurationDetails.configuration.effect))
        } else {
            configurationOptionsList.add(LightConfigurationItem.BrightnessConfigurationItem(lightConfigurationDetails.configuration.brightness))
            if (configurationMode == WHITE) {
                configurationOptionsList.add(LightConfigurationItem.TemperatureConfigurationItem(lightConfigurationDetails.configuration.temperature))
            } else if (configurationMode == COLORS) {
                configurationOptionsList.add(
                    LightConfigurationItem.ColorConfigurationItem(
                        lightConfigurationDetails.configuration.saturation,
                        Color.parseColor(lightConfigurationDetails.configuration.colorRgbHex)
                    )
                )
            }
        }

        return configurationOptionsList
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.updatePadding(top = insets.top, bottom = insets.bottom)
            setConfigurationsListMaxHeight()
            CONSUMED
        }
    }

    private fun setupViews() {
        (binding.root as? ViewGroup)?.layoutTransition = LayoutTransition().apply {
            enableTransitionType(CHANGING)
        }
        setupLightDetailsCard()
        setupNavigationBar()
    }

    private fun initLightConfigurationAdapter(): LightConfigurationAdapter =
        LightConfigurationAdapter(
            ::onLightConfigurationUpdated,
            ::onColorHexInputClick,
            ::onOpenCameraColorPickerClick,
            ::trackConfigurationChangedEvent
        )

    private fun onColorHexInputClick(currentColorHex: String) {
        dialogHandler.showColorHexInputDialog(requireContext(), currentColorHex) { inputColorHex ->
            val colorInt = Color.parseColor(inputColorHex)
            viewModel.updateLightConfiguration(
                LightConfigurationItem.ColorConfigurationItem(
                    colorInt.colorSaturation,
                    colorInt.colorWithMaxBrightness
                )
            )

            val lightConfigurationDetails = viewModel.selectedLightDetails.value ?: return@showColorHexInputDialog
            binding.lightPowerIV.setLightPowerButtonImage(lightConfigurationDetails.configuration, lightConfigurationDetails.status)
            lightConfigurationAdapter.submitList(getConfigurationOptionsList(lightConfigurationDetails.configuration.configurationMode))

            if (lightConfigurationDetails.configuration.isIndividualLight) {
                val sliteAddress = lightConfigurationDetails.address ?: return@showColorHexInputDialog
                val lightConfiguration = lightConfigurationDetails.configuration
                bluetoothService.setSliteOutputValue(
                    sliteAddress,
                    lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                )
            } else {
                lightConfigurationDetails.configuration.lightsDetails.forEach {
                    val sliteAddress = it.address ?: return@forEach
                    val lightConfiguration = lightConfigurationDetails.configuration
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        lightConfiguration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                    )
                }
            }
        }
    }

    private fun onOpenCameraColorPickerClick() {
        val lightDetailsConfiguration = viewModel.selectedLightDetails.value ?: return
        analyticsService.trackOpenCameraColorPickerMode()

        findNavController().navigate(
            LightConfigurationDialogFragmentDirections.actionLightConfigurationDialogFragmentToImageColorPickerFragment(
                lightDetailsConfiguration
            )
        )
    }

    private fun setupLightDetailsCard() {
        val lightConfigurationDetails = viewModel.selectedLightDetails.value ?: return

        binding.lightNameTV.setLightName(lightConfigurationDetails.configuration.name, lightConfigurationDetails.status)
        binding.sliteStatusTV.setLightStatusText(lightConfigurationDetails.configuration, lightConfigurationDetails.status)
        binding.lightPowerIV.setLightPowerButtonImage(lightConfigurationDetails.configuration, lightConfigurationDetails.status)
    }

    private fun setupNavigationBar() {
        val configurationMode = viewModel.selectedLightDetails.value?.configuration?.configurationMode ?: WHITE
        binding.configurationNavigationBar.selectConfigurationTab(configurationMode, false)
//        if (configurationMode == EFFECTS) {
//            handleEffectChanges()
//        }

        binding.configurationNavigationBar.setOnConfigurationSelectedListener { selectedConfigurationMode ->
            handleConfigurationModeChanged(selectedConfigurationMode)
            trackLightConfigurationModeChangedEvent()
        }
    }

    private fun trackLightConfigurationModeChangedEvent() {
        val lightConfiguration = viewModel.selectedLightDetails.value?.configuration ?: return
        analyticsService.trackAdvancedModeInteractions(lightConfiguration)
    }

    private fun trackConfigurationChangedEvent(lightConfigurationItem: LightConfigurationItem) {
        val lightDetails = viewModel.selectedLightDetails.value ?: return
        val updatedConfiguration = lightConfigurationItem.copyConfigurationWithUpdatedValues(lightDetails)
        analyticsService.trackAdvancedModeInteractions(updatedConfiguration)
    }

    private fun handleConfigurationModeChanged(selectedConfigurationMode: LightConfigurationMode, isFromBluetoothCommand: Boolean = false) {
        val lightConfigurationDetails = viewModel.selectedLightDetails.value ?: return

        if (selectedConfigurationMode == EFFECTS && !internalStorageManager.hasUserAcknowledgedPhotoSensitivityWarning) {
            findNavController().navigate(
                LightConfigurationDialogFragmentDirections.actionLightConfigurationDialogFragmentToPhotoSensitivityWarningFragment(
                    args.lightId,
                    lightConfigurationDetails.address,
                    lightConfigurationDetails.configuration
                )
            )
            return
        }
        binding.lightPowerIV.setLightPowerButtonImage(lightConfigurationDetails.configuration, lightConfigurationDetails.status)
        viewModel.updateLightConfigurationMode(selectedConfigurationMode)
        lightConfigurationAdapter.submitList(getConfigurationOptionsList(selectedConfigurationMode))

        if (isFromBluetoothCommand) return
        handleEffectChanges()
        if (selectedConfigurationMode != EFFECTS) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (lightConfigurationDetails.configuration.isIndividualLight) {
                    val sliteAddress = lightConfigurationDetails.address ?: return@postDelayed
                    bluetoothService.setSliteConfigurationMode(sliteAddress, selectedConfigurationMode)
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        lightConfigurationDetails.configuration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                    )
                } else {
                    lightConfigurationDetails.configuration.lightsDetails.forEach { light ->
                        val sliteAddress = light.address ?: return@postDelayed
                        bluetoothService.setSliteConfigurationMode(sliteAddress, selectedConfigurationMode)
                        bluetoothService.setSliteOutputValue(
                            sliteAddress,
                            lightConfigurationDetails.configuration.toSliteLightCharacteristic(SLITE_STATUS_ON)
                        )
                    }
                }
            }, LIGHT_CONFIGURATION_UPDATE_THROTTLE)
        }
    }

    private fun handleEffectChanges() {
        val lightConfiguration = viewModel.selectedLightDetails.value ?: return
        val effect = lightConfiguration.configuration.effect
        val isPoweredOn = lightConfiguration.status == ON && lightConfiguration.configuration.configurationMode == EFFECTS
        val sliteMode = if (isPoweredOn) EFFECTS else lightConfiguration.configuration.configurationMode

        if (lightConfiguration.configuration.isIndividualLight) {
            val sliteAddress = lightConfiguration.address ?: return
            bluetoothService.setSliteConfigurationMode(lightConfiguration.address, sliteMode)
            effectsHandler.updateEffectStatus(sliteAddress, effect, isPoweredOn)
        } else {
            val groupedLightsAddresses = lightConfiguration.configuration.lightsDetails.filter { it.status == ON }.mapNotNull { it.address }
            groupedLightsAddresses.forEach {
//                bluetoothService.setSliteOutputValue(it, lightConfiguration.configuration.toSliteLightCharacteristic(SLITE_STATUS_ON))
                bluetoothService.setSliteConfigurationMode(it, sliteMode)
            }
            effectsHandler.updateEffectStatus(groupedLightsAddresses, lightConfiguration.id, effect, isPoweredOn)
        }
    }

    @RequiresApi(TIRAMISU)
    private fun getHasNotificationsPermissions(): Boolean =
        requireContext().checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED

    private fun initRequestNotificationsPermissionLauncher(): ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                handleEffectChanges()
            } else {
                dialogHandler.showPermissionsRationaleDialog(
                    requireContext(),
                    "Notifications Permissions",
                    "Slite requires permission to show a notification when at least one Effect is ongoing." +
                            "\nYou will still be able to use Effects freely, but the related notification will not be displayed.",
                    onDismiss = ::handleEffectChanges
                )
            }
        }

    @RequiresApi(TIRAMISU)
    private fun handleNotificationPermissions() {
        if (!hasNotificationsPermission) {
            requestNotificationsPermissions()
        } else {
            handleEffectChanges()
        }
    }

    @RequiresApi(TIRAMISU)
    private fun requestNotificationsPermissions() {
        if (!shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {
            requestNotificationsPermissionLauncher.launch(POST_NOTIFICATIONS)
        } else {
            handleEffectChanges()
        }
    }

    private fun setConfigurationsListMaxHeight() {
        val configurationConstraintSet = ConstraintSet()
        configurationConstraintSet.clone(binding.configurationOptionsContainerCL)
        configurationConstraintSet.constrainMaxHeight(binding.lightConfigurationsContainerFL.id, calculateConfigurationsListMaxHeight())
        configurationConstraintSet.applyTo(binding.configurationOptionsContainerCL)
    }

    private fun calculateConfigurationsListMaxHeight(): Int {
        val screenHeight = requireActivity().window.decorView.height
        val navigationBarHeight = binding.configurationNavigationBar.layoutParams.height
        val lightCardHeight = binding.lightInfoContainerCL.layoutParams.height
        val actionBarsPadding = binding.root.paddingTop + binding.root.paddingBottom
        val viewsSpacing =
            binding.lightConfigurationsContainerFL.marginTop + binding.swipeLayout.marginTop + binding.dialogDragableIndicatorIV.marginTop
        val dragAreaHeight = binding.dialogDragableIndicatorIV.layoutParams.height
        val dialogSpacingTop = resources.getDimension(R.dimen.light_configuration_dialog_margin_top).roundToInt()

        return screenHeight - (navigationBarHeight + lightCardHeight + actionBarsPadding + viewsSpacing + dragAreaHeight + dialogSpacingTop)
    }

    override fun onDismiss(dialog: DialogInterface) {
        lightConfigurationDoneListener?.onLightConfigurationDone()
        super.onDismiss(dialog)
    }

    interface OnConfigurationDialogSwipeActionsClickListener {

        fun onConfigurationDialogRenameLightClick(lightId: Int, isLightsGroup: Boolean, currentLightName: String)

        fun onConfigurationDialogDeleteLightClick(lightId: Int, lightName: String, isLightsGroup: Boolean)
    }

    interface OnLightConfigurationDoneListener {

        fun onLightConfigurationDone()

        fun onLightConfigurationResumed()
    }

    private val swipeActionsListener: OnConfigurationDialogSwipeActionsClickListener?
        get() = fragmentsOnBackStack?.firstOrNull { it is OnConfigurationDialogSwipeActionsClickListener } as? OnConfigurationDialogSwipeActionsClickListener

    private val lightConfigurationDoneListener: OnLightConfigurationDoneListener?
        get() = fragmentsOnBackStack?.firstOrNull { it is OnLightConfigurationDoneListener } as? OnLightConfigurationDoneListener

    companion object {
        private val DialogFragment.fragmentsOnBackStack get() = ((parentFragment as? NavHostFragment)?.childFragmentManager?.fragments)
    }
}