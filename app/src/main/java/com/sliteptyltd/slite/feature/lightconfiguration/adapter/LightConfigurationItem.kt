package com.sliteptyltd.slite.feature.lightconfiguration.adapter

import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationAdapter.Companion.LIGHT_BRIGHTNESS_TYPE
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationAdapter.Companion.LIGHT_COLOR_TYPE
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationAdapter.Companion.LIGHT_EFFECTS_TYPE
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationAdapter.Companion.LIGHT_TEMPERATURE_TYPE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE

sealed class LightConfigurationItem(val itemId: Int) {

    data class BrightnessConfigurationItem(
        @IntRange(from = LIGHT_CONFIGURATION_MIN_BRIGHTNESS, to = LIGHT_CONFIGURATION_MAX_BRIGHTNESS)
        val brightness: Int
    ) : LightConfigurationItem(LIGHT_BRIGHTNESS_TYPE)

    data class TemperatureConfigurationItem(
        @IntRange(from = LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE, to = LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE)
        val temperature: Int
    ) : LightConfigurationItem(LIGHT_TEMPERATURE_TYPE)

    data class ColorConfigurationItem(
        @IntRange(from = LIGHT_CONFIGURATION_MIN_SATURATION, to = LIGHT_CONFIGURATION_MAX_SATURATION)
        val saturation: Int,
        @ColorInt
        val color: Int
    ) : LightConfigurationItem(LIGHT_COLOR_TYPE)

    data class EffectsConfigurationItem(
        var effect: Effect?
    ) : LightConfigurationItem(LIGHT_EFFECTS_TYPE)
}