package com.sliteptyltd.slite.utils.color

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.sliteptyltd.slite.utils.Constants.ColorUtils.COLOR_HEX_STRING_ARGB_FORMAT
import com.sliteptyltd.slite.utils.Constants.ColorUtils.COLOR_HEX_STRING_RGB_FORMAT
import com.sliteptyltd.slite.utils.Constants.ColorUtils.EFFECT_DICE_ROLL_MAX_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.EFFECT_DICE_ROLL_MIN_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.GREEN_FORMULA_DIFFERENTIATION_THRESHOLD
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_HUE_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_SATURATION_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_SIZE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_VALUE_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_SATURATION
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MIN_SATURATION
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_BLUE_TEMPERATURE_THRESHOLD
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_EFFECT_HUE_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_EFFECT_SATURATION_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_HUE_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_RED_TEMPERATURE_THRESHOLD
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_RGB_COMPONENT_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MIN_BLUE_TEMPERATURE_THRESHOLD
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MIN_EFFECT_HUE_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MIN_EFFECT_SATURATION_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MIN_RGB_COMPONENT_VALUE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Given a temperature in Kelvin, estimate an RGB equivalent
 * temperature in [1000, 10000]
 */
fun transformTemperatureToRGB(
    @IntRange(from = LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE, to = LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE)
    temperature: Int
): Int {
    val normalizedTemperature = temperature.normalizeIn()

    val red = if (normalizedTemperature <= MAX_RED_TEMPERATURE_THRESHOLD) {
        MAX_RGB_COMPONENT_VALUE
    } else {
        val temperatureRedValue = 329.698727446f * (normalizedTemperature - 60f).pow(-0.1332047592f)
        temperatureRedValue.asNormalizedRGBComponent
    }

    val green = if (normalizedTemperature <= GREEN_FORMULA_DIFFERENTIATION_THRESHOLD) {
        val temperatureGreenValue = 99.4708025861f * ln(normalizedTemperature) - 161.1195681661f
        temperatureGreenValue.asNormalizedRGBComponent
    } else {
        val temperatureGreenValue = 288.1221695283f * (normalizedTemperature - 60f).pow(-0.0755148492f)
        temperatureGreenValue.asNormalizedRGBComponent
    }

    val blue = when {
        normalizedTemperature >= MAX_BLUE_TEMPERATURE_THRESHOLD -> MAX_RGB_COMPONENT_VALUE
        normalizedTemperature <= MIN_BLUE_TEMPERATURE_THRESHOLD -> MIN_RGB_COMPONENT_VALUE
        else -> {
            val temperatureBlueValue = 138.5177312231f * ln(normalizedTemperature - 10) - 305.0447927307f
            temperatureBlueValue.asNormalizedRGBComponent
        }
    }

    return Color.rgb(red, green, blue)
}

fun getColorWithSaturation(
    @ColorInt color: Int,
    @IntRange(from = LIGHT_CONFIGURATION_MIN_SATURATION, to = LIGHT_CONFIGURATION_MAX_SATURATION)
    saturation: Int
): Int {
    val hsv = FloatArray(HSV_ARRAY_SIZE)
    Color.colorToHSV(color, hsv)
    return Color.HSVToColor(floatArrayOf(hsv[HSV_ARRAY_HUE_INDEX], saturation.normalizeIn(), hsv[HSV_ARRAY_VALUE_INDEX]))
}

val Color.red: Int get() = (MAX_RGB_COMPONENT_VALUE * red()).roundToInt()
val Color.green: Int get() = (MAX_RGB_COMPONENT_VALUE * green()).roundToInt()
val Color.blue: Int get() = (MAX_RGB_COMPONENT_VALUE * blue()).roundToInt()

val Int.colorSaturationHSV: Float
    get() {
        val hsv = FloatArray(HSV_ARRAY_SIZE)
        Color.colorToHSV(this, hsv)
        return hsv[HSV_ARRAY_SATURATION_INDEX]
    }

val Int.colorValueHSV: Float
    get() {
        val hsv = FloatArray(HSV_ARRAY_SIZE)
        Color.colorToHSV(this, hsv)
        return hsv[HSV_ARRAY_VALUE_INDEX]
    }

val Int.colorHue: Float
    get() {
        val hsv = FloatArray(HSV_ARRAY_SIZE)
        Color.colorToHSV(this, hsv)
        return hsv[HSV_ARRAY_HUE_INDEX]
    }

val Int.colorValue: Int get() = (colorValueHSV * 100).roundToInt()
val Int.colorSaturation: Int get() = (colorSaturationHSV * 100).roundToInt()
val Int.colorWithMinSaturation: Int get() = Color.HSVToColor(floatArrayOf(colorHue, HSV_MIN_SATURATION, HSV_MAX_VALUE))
val Int.colorWithMaxSaturation: Int get() = Color.HSVToColor(floatArrayOf(colorHue, HSV_MAX_SATURATION, HSV_MAX_VALUE))

val Int.colorWithMaxBrightness: Int get() = Color.HSVToColor(floatArrayOf(colorHue, colorSaturationHSV, HSV_MAX_VALUE))

fun Int.toArgbHexString(): String =
    String.format(
        COLOR_HEX_STRING_ARGB_FORMAT,
        (0xFFFFFFFF and Color.valueOf(this).toArgb().toLong())
    )

fun Int.toRgbHexString(): String =
    String.format(
        COLOR_HEX_STRING_RGB_FORMAT,
        (0xFFFFFF and Color.valueOf(this).toArgb())
    )

private val Float.asNormalizedRGBComponent: Int
    get() = when {
        this < MIN_RGB_COMPONENT_VALUE -> MIN_RGB_COMPONENT_VALUE
        this > MAX_RGB_COMPONENT_VALUE -> MAX_RGB_COMPONENT_VALUE
        else -> this.roundToInt()
    }

fun getHueHorizontalGradientDrawable(drawableCornerRadius: Float, saturation: Float = HSV_MAX_SATURATION): GradientDrawable {
    val hueArray = IntArray(MAX_HUE_VALUE.toInt()) { hueValue ->
        val colors = floatArrayOf(hueValue.toFloat(), saturation, HSV_MAX_VALUE)
        Color.HSVToColor(colors)
    }
    return GradientDrawable(
        LEFT_RIGHT,
        hueArray
    ).apply { cornerRadius = drawableCornerRadius }
}

fun getRandomHue(): Float = Random.nextInt(MIN_EFFECT_HUE_VALUE, MAX_EFFECT_HUE_VALUE + 1).toFloat()

fun getRandomSaturation(): Int = Random.nextInt(MIN_EFFECT_SATURATION_VALUE, MAX_EFFECT_SATURATION_VALUE + 1)

fun getEffectPhaseDiceRoll(): Int = Random.nextInt(EFFECT_DICE_ROLL_MIN_VALUE, EFFECT_DICE_ROLL_MAX_VALUE + 1)