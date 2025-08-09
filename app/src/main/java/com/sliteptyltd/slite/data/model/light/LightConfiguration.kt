package com.sliteptyltd.slite.data.model.light

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.IntRange
import androidx.annotation.Keep
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_VALUE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_HUE
import com.sliteptyltd.slite.utils.color.toArgbHexString
import com.sliteptyltd.slite.utils.color.transformTemperatureToRGB
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LightConfiguration(
    val name: String,
    val hue: Float = DEFAULT_LIGHT_HUE,
    @IntRange(from = LIGHT_CONFIGURATION_MIN_BRIGHTNESS, to = LIGHT_CONFIGURATION_MAX_BRIGHTNESS)
    val brightness: Int = DEFAULT_LIGHT_BRIGHTNESS,
    @IntRange(from = LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE, to = LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE)
    val temperature: Int = DEFAULT_LIGHT_TEMPERATURE,
    @IntRange(from = LIGHT_CONFIGURATION_MIN_SATURATION, to = LIGHT_CONFIGURATION_MAX_SATURATION)
    val saturation: Int = DEFAULT_LIGHT_SATURATION,
    val effect: Effect? = null,
    val configurationMode: LightConfigurationMode = WHITE,
    val lightsDetails: List<GroupLightDetails> = listOf()
) : Parcelable {

    val isLightsGroup: Boolean get() = lightsDetails.isNotEmpty()

    val isIndividualLight: Boolean get() = lightsDetails.isEmpty()

    val colorRgbHex: String
        get() = if (configurationMode == WHITE) {
            transformTemperatureToRGB(temperature).toArgbHexString()
        } else {
            Color.HSVToColor(floatArrayOf(hue, saturation.normalizeIn(), HSV_MAX_VALUE)).toArgbHexString()
        }
}