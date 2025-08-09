package com.sliteptyltd.slite.analytics

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.sliteptyltd.slite.BuildConfig.DEBUG
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic.Companion.HUE_MAPPING_OFFSET
import org.json.JSONObject

class AnalyticsService {

    private var mixpanel: MixpanelAPI? = null

    fun initAnalyticsService(context: Context) {
        if (!DEBUG) {
            mixpanel = MixpanelAPI.getInstance(context, PROJECT_TOKEN)
//            mixpanel?.setEnableLogging(true)
            mixpanel?.distinctId?.let { distinctId -> mixpanel?.identify(distinctId) }
        }
    }

    fun trackAddLight() {
        trackEvent(CREATE_LIGHT_EVENT_NAME)
    }

    fun trackLightBlackoutStateChanged(isBlackout: Boolean) {
        val blackoutStateChangedEventName = if (isBlackout) LIGHT_POWER_OFF_EVENT_NAME else LIGHT_POWER_ON_EVENT_NAME
        trackEvent(blackoutStateChangedEventName)
    }

    fun trackCreateScene() {
        trackEvent(SCENE_CREATED_EVENT_NAME)
    }

    fun trackApplyScene() {
        trackEvent(SCENE_APPLIED_EVENT_NAME)
    }

    fun trackCreateGroup() {
        trackEvent(CREATE_GROUP_EVENT_NAME)
    }

    fun trackGroupBlackoutStateChanged(isBlackout: Boolean) {
        val blackoutStateChangedEventName = if (isBlackout) GROUP_POWER_OFF_EVENT_NAME else GROUP_POWER_ON_EVENT_NAME
        trackEvent(blackoutStateChangedEventName)
    }

    fun trackOpenCameraColorPickerMode() {
        trackEvent(CAMERA_COLOR_PICKER_EVENT_NAME)
    }

    fun trackOpenGalleryColorPickerMode() {
        trackEvent(GALLERY_COLOR_PICKER_EVENT_NAME)
    }

    fun trackUseSelectedColorEvent(hue: Int, saturation: Int, isCameraMode: Boolean) {
        val selectedColorEventProperties = JSONObject().apply {
            put(USE_SELECTED_COLOR_HUE_PROPERTY_NAME, hue)
            put(USE_SELECTED_COLOR_SATURATION_PROPERTY_NAME, saturation)
        }

        val eventName = if (isCameraMode) {
            CAMERA_COLOR_PICKER_EVENT_NAME
        } else {
            GALLERY_COLOR_PICKER_EVENT_NAME
        }

        trackEvent(eventName, selectedColorEventProperties)
    }

    fun trackAdvancedModeInteractions(lightConfiguration: LightConfiguration) {
        when (lightConfiguration.configurationMode) {
            WHITE -> trackWhiteModeInteractions(lightConfiguration.brightness, lightConfiguration.temperature)
            COLORS -> trackColorModeInteractions(
                lightConfiguration.brightness,
                lightConfiguration.hue.toInt() + HUE_MAPPING_OFFSET.toInt(),
                lightConfiguration.saturation
            )
            EFFECTS -> trackEffectModeInteractions(lightConfiguration.effect?.toString())
        }
    }

    private fun trackWhiteModeInteractions(brightness: Int, temperature: Int) {
        val whiteModeProperties = JSONObject().apply {
            put(BRIGHTNESS_VALUE_PROPERTY_NAME, brightness)
            put(KELVIN_VALUE_PROPERTY_NAME, temperature)
        }

        trackEvent(ADVANCED_MODE_WHITE_EVENT_NAME, whiteModeProperties)
    }

    private fun trackColorModeInteractions(brightness: Int, hue: Int, saturation: Int) {
        val colorModeProperties = JSONObject().apply {
            put(BRIGHTNESS_VALUE_PROPERTY_NAME, brightness)
            put(HUE_VALUE_PROPERTY_NAME, hue)
            put(SATURATION_VALUE_PROPERTY_NAME, saturation)
        }

        trackEvent(ADVANCED_MODE_COLOR_EVENT_NAME, colorModeProperties)
    }

    private fun trackEffectModeInteractions(effectName: String?) {
        val colorModeProperties = JSONObject().apply {
            put(EFFECT_NAME_PROPERTY_NAME, effectName)
        }

        trackEvent(ADVANCED_MODE_EFFECTS_EVENT_NAME, colorModeProperties)
    }

    private fun trackEvent(eventName: String, properties: JSONObject? = null) {
        if (properties != null) {
            mixpanel?.track(eventName, properties)
        } else {
            mixpanel?.track(eventName)
        }
    }

    companion object {
        private const val PROJECT_TOKEN = "a4e9e25417a643291c512cd775e446cd"

        private const val CREATE_LIGHT_EVENT_NAME = "Light added successfully"
        private const val LIGHT_POWER_ON_EVENT_NAME = "Light turned on"
        private const val LIGHT_POWER_OFF_EVENT_NAME = "Light turned off"

        private const val SCENE_CREATED_EVENT_NAME = "Scene created successfully"
        private const val SCENE_APPLIED_EVENT_NAME = "Scene applied successfully"

        private const val CREATE_GROUP_EVENT_NAME = "Group created successfully"
        private const val GROUP_POWER_ON_EVENT_NAME = "Group turned on"
        private const val GROUP_POWER_OFF_EVENT_NAME = "Group turned off"

        private const val CAMERA_COLOR_PICKER_EVENT_NAME = "Photo color picker usage"
        private const val GALLERY_COLOR_PICKER_EVENT_NAME = "Camera color picker usage"
        private const val USE_SELECTED_COLOR_HUE_PROPERTY_NAME = "hue"
        private const val USE_SELECTED_COLOR_SATURATION_PROPERTY_NAME = "saturation"

        private const val ADVANCED_MODE_WHITE_EVENT_NAME = "Advanced mode - white"
        private const val ADVANCED_MODE_COLOR_EVENT_NAME = "Advanced mode - color"
        private const val ADVANCED_MODE_EFFECTS_EVENT_NAME = "Advanced mode - effect"

        private const val BRIGHTNESS_VALUE_PROPERTY_NAME = "Brightness value set"
        private const val KELVIN_VALUE_PROPERTY_NAME = "Kelvin value set"
        private const val HUE_VALUE_PROPERTY_NAME = "Hue value set"
        private const val SATURATION_VALUE_PROPERTY_NAME = "Saturation value set"
        private const val EFFECT_NAME_PROPERTY_NAME = "EffectName"
    }
}