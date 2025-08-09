package com.sliteptyltd.slite.feature.lights.adapter.viewholder

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.MotionEvent.ACTION_UP
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.daimajia.swipe.SwipeLayout
import com.sliteptyltd.slite.LightItemBinding
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.feature.lights.adapter.CloseAllSwipeableLayoutsCallback
import com.sliteptyltd.slite.feature.lights.adapter.OnDeleteLightClick
import com.sliteptyltd.slite.feature.lights.adapter.OnLightConfigurationButtonClick
import com.sliteptyltd.slite.feature.lights.adapter.OnLightConfigurationUpdated
import com.sliteptyltd.slite.feature.lights.adapter.OnLightPowerButtonClick
import com.sliteptyltd.slite.feature.lights.adapter.OnRenameLightClick
import com.sliteptyltd.slite.feature.lights.adapter.OnTrackConfigurationChangedEvent
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_HUE_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MIN_HUE_VALUE
import com.sliteptyltd.slite.utils.Constants.Lights.DROPDOWN_COLLAPSED_ROTATION
import com.sliteptyltd.slite.utils.Constants.Lights.DROPDOWN_EXPANDED_ROTATION
import com.sliteptyltd.slite.utils.Constants.Lights.EXPAND_LIGHT_ITEM_ANIMATION_DURATION
import com.sliteptyltd.slite.utils.Constants.Lights.GRADIENT_TRACK_HEIGHT_DIFFERENCE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_ITEM_PERCENT_TEXT_FORMAT
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_TEMPERATURE_TEXT_FORMAT
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.getHueHorizontalGradientDrawable
import com.sliteptyltd.slite.utils.color.transformTemperatureToRGB
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import com.sliteptyltd.slite.utils.extensions.views.animateHeightChange
import com.sliteptyltd.slite.utils.extensions.views.rotateDropdownButton
import com.sliteptyltd.slite.utils.extensions.views.updateLayoutParamsHeight
import com.sliteptyltd.slite.views.swipelayout.SwipeListener
import kotlin.math.floor
import kotlin.math.roundToInt

class LightItemViewHolder(
    private val binding: LightItemBinding,
    private val onRenameLightClick: OnRenameLightClick,
    private val onDeleteLightClick: OnDeleteLightClick,
    private val onLightPowerButtonClick: OnLightPowerButtonClick,
    private val onLightConfigurationButtonClick: OnLightConfigurationButtonClick,
    private val onLightConfigurationUpdated: OnLightConfigurationUpdated,
    private val onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent,
    private val closeAllSwipeableLayoutsCallback: CloseAllSwipeableLayoutsCallback
) : LightsListItemViewHolder(binding) {

    private var lightDetails: LightsListItem.LightItem? = null
    private var shouldNotify = false

    init {
        initBrightnessSlider()
        initSecondarySlider()
        initListeners()
        initTrackingListeners()
    }

    override fun bind(lightsListItem: LightsListItem) {
        val lightDetails = (lightsListItem as? LightsListItem.LightItem)
            ?: throw IllegalArgumentException("Item of type ${lightsListItem::class.java} could not be cast to LightsListItem.LightsListItem")

        shouldNotify = false
        this.lightDetails = lightDetails
        binding.lightDetails = lightDetails

        if (lightDetails.lightConfiguration.configurationMode == EFFECTS) {
            binding.lightControlsGroup.isVisible = false
            binding.secondaryPropertyValueTV.isVisible = false
            binding.configButtonSeparator.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin =
                    binding.root.resources.getDimension(R.dimen.lights_list_section_control_power_button_margin_vertical).roundToInt()
            }

        } else {
            binding.lightControlsGroup.isVisible = lightDetails.lightUIConfiguration.isExpanded
            binding.secondaryPropertyValueTV.isVisible = lightDetails.lightUIConfiguration.isExpanded
            binding.configButtonSeparator.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = binding.root.resources.getDimension(R.dimen.lights_list_light_item_separator_margin_top).roundToInt()
            }
            setupBrightnessSlider(lightDetails.lightConfiguration)
            setupSecondarySlider(lightDetails.lightConfiguration)
        }
        binding.advancedOptionsGroup.isVisible = lightDetails.lightUIConfiguration.isExpanded
        setupSwipeLayout(lightDetails)
        setupDropdownRotation(lightDetails.lightUIConfiguration.isExpanded)

        binding.lightInfoContainerCL.updateLayoutParamsHeight(getContainerHeight(lightDetails.lightUIConfiguration.isExpanded))
        shouldNotify = true
    }

    private fun initBrightnessSlider() {
        binding.brightnessSlider.setCustomThumbDrawable(R.drawable.ic_brightness_slider_thumb)
        binding.brightnessSlider.addOnChangeListener { _, value, _ ->
            val lightDetails = lightDetails ?: return@addOnChangeListener
            if (!shouldNotify) return@addOnChangeListener
            val selectedBrightness = value.toInt()
            binding.brightnessValueTV.text = String.format(LIGHT_ITEM_PERCENT_TEXT_FORMAT, selectedBrightness)

            val updatedConfiguration = lightDetails.lightConfiguration.copyWithUpdatedValues(
                brightness = selectedBrightness,
                status = lightDetails.status
            )
            this.lightDetails = lightDetails.copy(lightConfiguration = updatedConfiguration)
            binding.lightDetails = this.lightDetails
            onLightConfigurationUpdated(lightDetails.itemId, updatedConfiguration)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTrackingListeners() {
        binding.brightnessSlider.setOnTouchListener { _, motionEvent ->
            if (motionEvent?.action == ACTION_UP) trackConfigurationChange()
            return@setOnTouchListener false
        }

        binding.secondaryPropertySlider.setOnTouchListener { _, motionEvent ->
            if (motionEvent?.action == ACTION_UP) trackConfigurationChange()
            return@setOnTouchListener false
        }
    }

    private fun trackConfigurationChange() {
        val lightId = lightDetails?.itemId ?: return
        onTrackConfigurationChangedEvent(lightId)
    }

    private fun initSecondarySlider() {
        binding.secondaryPropertySlider.addOnChangeListener { _, value, _ ->
            if (!shouldNotify) return@addOnChangeListener
            val selectedValue = value.toInt()
            when (lightDetails?.lightConfiguration?.configurationMode) {
                WHITE -> onTemperatureChanged(selectedValue)
                COLORS -> onHueChanged(selectedValue)
                else -> binding.secondaryPropertyValueTV.text = ""
            }
        }
        binding.hueGradient.updateLayoutParams {
            height =
                (binding.root.resources.getDimension(R.dimen.light_configuration_slider_track_height) + GRADIENT_TRACK_HEIGHT_DIFFERENCE).roundToInt()
        }
    }

    private fun onTemperatureChanged(temperature: Int) {
        val lightDetails = lightDetails ?: return
        binding.secondaryPropertyValueTV.text = String.format(LIGHT_TEMPERATURE_TEXT_FORMAT, temperature)
        binding.lightPowerIV.backgroundTintList = ColorStateList.valueOf(transformTemperatureToRGB(temperature))

        val updatedConfiguration = lightDetails.lightConfiguration.copyWithUpdatedValues(
            temperature = temperature,
            status = lightDetails.status
        )
        this.lightDetails = lightDetails.copy(lightConfiguration = updatedConfiguration)
        binding.lightDetails = this.lightDetails
        onLightConfigurationUpdated(lightDetails.itemId, updatedConfiguration)
    }

    private fun onHueChanged(hue: Int) {
        val saturation = lightDetails?.lightConfiguration?.saturation?.toFloat() ?: return
        val lightDetails = lightDetails ?: return
        val color = Color.HSVToColor(floatArrayOf(hue.toFloat(), saturation.normalizeIn(), HSV_MAX_VALUE))

        binding.lightPowerIV.backgroundTintList = ColorStateList.valueOf(color)

        val updatedConfiguration = lightDetails.lightConfiguration.copyWithUpdatedValues(
            hue = hue.toFloat(),
            status = lightDetails.status
        )
        this.lightDetails = lightDetails.copy(lightConfiguration = updatedConfiguration)
        binding.lightDetails = this.lightDetails
        onLightConfigurationUpdated(lightDetails.itemId, updatedConfiguration)
    }

    private fun setupBrightnessSlider(lightConfiguration: LightConfiguration) {
        binding.brightnessSlider.value = lightConfiguration.brightness.toFloat()
        binding.brightnessValueTV.text = String.format(LIGHT_ITEM_PERCENT_TEXT_FORMAT, lightConfiguration.brightness)
    }

    private fun setupSecondarySlider(lightConfiguration: LightConfiguration) {
        when (lightConfiguration.configurationMode) {
            WHITE -> setupSecondarySliderForTemperature(lightConfiguration.temperature.toFloat())
            COLORS -> setupSecondarySliderForHue(Color.parseColor(lightConfiguration.colorRgbHex), lightConfiguration.saturation)
            else -> Unit
        }
    }

    private fun setupSecondarySliderForTemperature(temperature: Float) {
        binding.secondaryPropertySlider.setCustomThumbDrawable(R.drawable.ic_temperature_slider_thumb)
        binding.secondaryPropertySlider.stepSize = LIGHT_CONFIGURATION_SLIDER_STEP_SIZE_TEMPERATURE
        binding.secondaryPropertySlider.valueFrom = LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE.toFloat()
        binding.secondaryPropertySlider.valueTo = LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE.toFloat()
        binding.secondaryPropertySlider.value = temperature

        binding.secondaryPropertySlider.trackActiveTintList =
            ColorStateList.valueOf(binding.root.resources.getColor(R.color.light_configuration_track_active_color, null))
        binding.secondaryPropertySlider.trackInactiveTintList =
            ColorStateList.valueOf(binding.root.resources.getColor(R.color.light_configuration_track_inactive_color, null))

        binding.secondaryPropertyValueTV.isVisible = lightDetails?.lightUIConfiguration?.isExpanded == true
        binding.secondaryPropertyValueTV.text = String.format(LIGHT_TEMPERATURE_TEXT_FORMAT, temperature.toInt())
        binding.secondaryPropertySlider.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = 0
        }
        binding.hueGradient.isInvisible = true
    }

    private fun setupSecondarySliderForHue(color: Int, saturation: Int) {
        binding.secondaryPropertySlider.setCustomThumbDrawable(R.drawable.ic_saturation_slider_thumb)
        binding.secondaryPropertySlider.stepSize = LIGHT_CONFIGURATION_SLIDER_STEP_SIZE_HUE
        binding.secondaryPropertySlider.valueFrom = MIN_HUE_VALUE
        binding.secondaryPropertySlider.valueTo = MAX_HUE_VALUE
        binding.secondaryPropertySlider.value = floor(color.colorHue)

        binding.secondaryPropertySlider.trackActiveTintList =
            ColorStateList.valueOf(binding.root.resources.getColor(R.color.light_configuration_hue_track_color, null))
        binding.secondaryPropertySlider.trackInactiveTintList =
            ColorStateList.valueOf(binding.root.resources.getColor(R.color.light_configuration_hue_track_color, null))

        binding.secondaryPropertyValueTV.isVisible = false
        binding.hueGradient.isInvisible = false

        binding.secondaryPropertySlider.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = binding.root.resources.getDimension(R.dimen.lights_list_light_item_hue_slider_margin_start).roundToInt()
        }
        binding.hueGradient.background = getHueHorizontalGradientDrawable(
            binding.root.resources.getDimension(R.dimen.light_configuration_gradient_track_corner_radius),
            saturation.normalizeIn()
        )
    }

    private fun setupSwipeLayout(lightDetails: LightsListItem.LightItem) {
        binding.swipeLayout.isRightSwipeEnabled = !lightDetails.lightUIConfiguration.isExpanded
        if (lightDetails.lightUIConfiguration.isActionsMenuOpen) {
            binding.swipeLayout.open(false)
        } else {
            binding.swipeLayout.close(false)
        }
    }

    private fun setupDropdownRotation(isExpanded: Boolean) {
        binding.dropdownIV.rotation = if (isExpanded) DROPDOWN_EXPANDED_ROTATION else DROPDOWN_COLLAPSED_ROTATION
    }

    private fun initListeners() {
        binding.dropdownContainerFL.setOnClickListener {
            onDropdownClick()
        }

        binding.swipeLayout.addSwipeListener(object : SwipeListener() {
            override fun onOpen(layout: SwipeLayout?) {
                lightDetails?.lightUIConfiguration?.isActionsMenuOpen = true
            }

            override fun onClose(layout: SwipeLayout?) {
                lightDetails?.lightUIConfiguration?.isActionsMenuOpen = false
            }
        })

        binding.editSliteNameBtn.root.setOnClickListener {
            val lightDetails = this.lightDetails ?: return@setOnClickListener

            binding.swipeLayout.close()
            this.lightDetails?.lightUIConfiguration?.isActionsMenuOpen = false

            onRenameLightClick(
                lightDetails.itemId,
                lightDetails.lightConfiguration.isLightsGroup,
                lightDetails.lightConfiguration.name
            )
        }

        binding.deleteSliteBtn.root.setOnClickListener {
            val lightDetails = this.lightDetails ?: return@setOnClickListener

            binding.swipeLayout.close()
            this.lightDetails?.lightUIConfiguration?.isActionsMenuOpen = false

            onDeleteLightClick(
                lightDetails.itemId,
                lightDetails.lightConfiguration.name,
                lightDetails.lightConfiguration.isLightsGroup
            )
        }

        binding.lightPowerIV.setOnClickListener {
            val lightDetails = this.lightDetails ?: return@setOnClickListener

            if (lightDetails.status == LightStatus.ON && lightDetails.lightUIConfiguration.isExpanded) {
                binding.lightInfoContainerCL.animateHeightChange(
                    binding.lightInfoContainerCL.height,
                    binding.root.resources.getDimension(R.dimen.lights_list_light_item_collapsed_height).roundToInt(),
                    EXPAND_LIGHT_ITEM_ANIMATION_DURATION,
                    onAnimationStart = { onExpandAnimationStart(false) },
                    onAnimationEnd = {
                        onExpandAnimationEnd(false)
                        onLightPowerButtonClick(lightDetails.itemId, lightDetails.status)
                    }
                )
            } else {
                onLightPowerButtonClick(lightDetails.itemId, lightDetails.status)
            }
        }

        binding.openAdvancedSettingsBtn.setOnClickListener {
            val lightDetails = this.lightDetails ?: return@setOnClickListener
            onLightConfigurationButtonClick(lightDetails.itemId)
        }
    }

    private fun onDropdownClick() {
        val isExpanded = lightDetails?.lightUIConfiguration?.isExpanded ?: false
        val startHeight = getContainerHeight(isExpanded)
        val endHeight = getContainerHeight(!isExpanded)

        binding.swipeLayout.isRightSwipeEnabled = isExpanded
        closeAllSwipeableLayoutsCallback()

        binding.lightInfoContainerCL.animateHeightChange(
            startHeight,
            endHeight,
            EXPAND_LIGHT_ITEM_ANIMATION_DURATION,
            onAnimationStart = { onExpandAnimationStart(isExpanded) },
            onAnimationEnd = { onExpandAnimationEnd(isExpanded) }
        )
    }

    private fun onExpandAnimationStart(isExpanded: Boolean) {
        if (!isExpanded) {
            binding.dropdownIV.rotateDropdownButton(DROPDOWN_EXPANDED_ROTATION)
            lightDetails?.lightUIConfiguration?.isExpanded = true
            if (lightDetails?.lightConfiguration?.configurationMode != EFFECTS) {
                binding.lightControlsGroup.isVisible = true
                binding.secondaryPropertyValueTV.isVisible = true && lightDetails?.lightConfiguration?.configurationMode == WHITE
            }
            binding.advancedOptionsGroup.isVisible = true
        }
    }

    private fun onExpandAnimationEnd(isExpanded: Boolean) {
        if (isExpanded) {
            binding.dropdownIV.rotateDropdownButton(DROPDOWN_COLLAPSED_ROTATION)
            lightDetails?.lightUIConfiguration?.isExpanded = false
            if (lightDetails?.lightConfiguration?.configurationMode != EFFECTS) {
                binding.lightControlsGroup.isVisible = false
                binding.secondaryPropertyValueTV.isVisible = false
            }
            binding.advancedOptionsGroup.isVisible = false
        }
    }

    private fun getContainerHeight(isExpanded: Boolean) =
        if (isExpanded) {
            if (lightDetails?.lightConfiguration?.configurationMode == EFFECTS) {
                binding.root.resources.getDimension(R.dimen.lights_list_light_item_effects_expanded_height).roundToInt()
            } else {
                binding.root.resources.getDimension(R.dimen.lights_list_light_item_expanded_height).roundToInt()
            }
        } else {
            binding.root.resources.getDimension(R.dimen.lights_list_light_item_collapsed_height).roundToInt()
        }

    private fun LightConfiguration.copyWithUpdatedValues(
        hue: Float? = null,
        brightness: Int? = null,
        temperature: Int? = null,
        saturation: Int? = null,
        status: LightStatus? = null
    ): LightConfiguration = copy(
        hue = hue ?: this.hue,
        brightness = brightness ?: this.brightness,
        temperature = temperature ?: this.temperature,
        saturation = saturation ?: this.saturation,
        lightsDetails = this.lightsDetails.map {
            val newStatus = if (it.status != LightStatus.DISCONNECTED) {
                status
            } else {
                LightStatus.DISCONNECTED
            }
            it.copy(status = newStatus ?: it.status)
        }
    )

    companion object {
        private const val LIGHT_CONFIGURATION_SLIDER_STEP_SIZE_TEMPERATURE = 100f
        private const val LIGHT_CONFIGURATION_SLIDER_STEP_SIZE_HUE = 1f
    }
}