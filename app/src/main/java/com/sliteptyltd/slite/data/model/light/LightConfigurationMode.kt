package com.sliteptyltd.slite.data.model.light

import androidx.annotation.Keep
import com.sliteptyltd.slite.utils.bluetooth.SliteBleManager.Companion.colorConfigurationModeSliteValues
import com.sliteptyltd.slite.utils.bluetooth.SliteBleManager.Companion.whiteConfigurationModeSliteValues

@Keep
enum class LightConfigurationMode {
    WHITE, COLORS, EFFECTS;

    companion object {

        fun fromSliteStatusCharacteristic(sliteStatusCharacteristicValue: String): LightConfigurationMode {
            if (sliteStatusCharacteristicValue.isEmpty()) return COLORS

            return when (sliteStatusCharacteristicValue.split(",").firstOrNull()) {
                in colorConfigurationModeSliteValues -> COLORS
                in whiteConfigurationModeSliteValues -> WHITE
                else -> EFFECTS
            }
        }
    }
}