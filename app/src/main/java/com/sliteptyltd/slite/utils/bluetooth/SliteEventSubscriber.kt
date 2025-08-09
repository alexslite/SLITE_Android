package com.sliteptyltd.slite.utils.bluetooth

import androidx.annotation.ColorInt
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.version.SliteVersion
import com.sliteptyltd.slite.utils.Constants.Connectivity.SLITE_CHARACTERISTICS_SEPARATOR
import com.sliteptyltd.slite.utils.Constants.Connectivity.SLITE_CHARACTERISTIC_BLACKOUT_INDEX
import com.sliteptyltd.slite.utils.Constants.Connectivity.SLITE_CHARACTERISTIC_BRIGHTNESS_INDEX
import com.sliteptyltd.slite.utils.Constants.Connectivity.SLITE_CHARACTERISTIC_HUE_INDEX
import com.sliteptyltd.slite.utils.Constants.Connectivity.SLITE_CHARACTERISTIC_SATURATION_INDEX
import com.sliteptyltd.slite.utils.Constants.Connectivity.SLITE_CHARACTERISTIC_TEMPERATURE_INDEX
import com.sliteptyltd.slite.utils.Constants.Effects.DAYLIGHT_WHITE_KELVIN_VALUE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_BLACKOUT_STATE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_HUE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_TEMPERATURE
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_ON
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateState
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturation
import com.sliteptyltd.slite.utils.color.colorValue
import com.sliteptyltd.slite.utils.logMe

open class SliteEventSubscriber {

    open fun onConnectionStateChanged(address: String, isConnected: Boolean) = Unit

    open fun onLightCharacteristicChanged(address: String, lightCharacteristic: SliteLightCharacteristic) = Unit

    open fun onLightConfigurationModeChanged(address: String, lightConfigurationMode: LightConfigurationMode) = Unit

    open fun onUpdateStateChanged(address: String, updateState: UpdateState) = Unit
}

data class SliteLightCharacteristic(
    val temperature: Int,
    val hue: Float,
    val saturation: Float,
    val brightness: Float,
    val blackout: Byte
) {

    companion object {
        fun SliteLightCharacteristic.isDefaultConfiguration(): Boolean =
            hue == DEFAULT_LIGHT_HUE &&
                    brightness.toInt() == DEFAULT_LIGHT_BRIGHTNESS &&
                    temperature == DEFAULT_LIGHT_TEMPERATURE &&
                    saturation.toInt() == DEFAULT_LIGHT_SATURATION

        fun SliteLightCharacteristic.toCharacteristicCSV(): String =
            String.format(
                LIGHT_CHARACTERISTIC_CSV_STRING_TEMPLATE,
                temperature,
                hue.toInt() + HUE_MAPPING_OFFSET.toInt(),
                saturation.toInt(),
                brightness.toInt(),
                blackout.toInt()
            )

        fun SliteLightCharacteristic.toEffectCharacteristicCSV(): String =
            String.format(
                LIGHT_EFFECT_CHARACTERISTIC_CSV_STRING_TEMPLATE,
                temperature,
                hue.toInt() + HUE_MAPPING_OFFSET.toInt(),
                saturation.toInt(),
                brightness.toInt()
            )

        fun fromColorInt(@ColorInt color: Int): SliteLightCharacteristic =
            SliteLightCharacteristic(
                DAYLIGHT_WHITE_KELVIN_VALUE,
                color.colorHue,
                color.colorSaturation.toFloat(),
                color.colorValue.toFloat(),
                SLITE_STATUS_ON
            )

        fun fromCharacteristicByteArray(characteristicValue: ByteArray): SliteLightCharacteristic =
            with(String(characteristicValue).split(SLITE_CHARACTERISTICS_SEPARATOR)) {
                try {
                    SliteLightCharacteristic(
                        this[SLITE_CHARACTERISTIC_TEMPERATURE_INDEX].toInt(),
                        this[SLITE_CHARACTERISTIC_HUE_INDEX].toFloat() - HUE_MAPPING_OFFSET,
                        this[SLITE_CHARACTERISTIC_SATURATION_INDEX].toFloat(),
                        this[SLITE_CHARACTERISTIC_BRIGHTNESS_INDEX].toFloat(),
                        this[SLITE_CHARACTERISTIC_BLACKOUT_INDEX].toByte()
                    )
                } catch (e: Exception) {
                    logMe("fromCharacteristicByteArray") { e.message.toString() + "with array" + characteristicValue.toString() }
                    SliteLightCharacteristic(
                        DEFAULT_LIGHT_TEMPERATURE,
                        DEFAULT_LIGHT_HUE,
                        DEFAULT_LIGHT_SATURATION.toFloat(),
                        DEFAULT_LIGHT_BRIGHTNESS.toFloat(),
                        DEFAULT_LIGHT_BLACKOUT_STATE
                    )
                }
            }

        const val HUE_MAPPING_OFFSET = 1f
        private const val LIGHT_CHARACTERISTIC_CSV_STRING_TEMPLATE = "%d,%d,%d,%d,%d"
        private const val LIGHT_EFFECT_CHARACTERISTIC_CSV_STRING_TEMPLATE = "%d,%d,%d,%d"
    }
}
