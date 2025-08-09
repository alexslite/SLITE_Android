package com.sliteptyltd.slite.feature.lightconfiguration.imagecolor

import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import com.sliteptyltd.slite.data.usecase.UpdateLightUseCase
import com.sliteptyltd.slite.feature.lightconfiguration.LightConfigurationDetails
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_CONFIGURATION_MAX_BRIGHTNESS
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturation
import com.sliteptyltd.slite.utils.color.toRgbHexString
import java.util.concurrent.atomic.AtomicBoolean

class ImageColorPickerViewModel(
    private val lightConfigurationDetails: LightConfigurationDetails,
    private val updateLight: UpdateLightUseCase
) : ViewModel() {

    var isTargetedColorLockedIn: AtomicBoolean = AtomicBoolean(false)
    var shouldCheckPermissions = true
    var isInCameraMode = true

    fun updateLightColor(@ColorInt selectedColorInt: Int) {
        val selectedColorSaturation = selectedColorInt.colorSaturation

        val updateLightDetails = lightConfigurationDetails.copy(
            configuration = lightConfigurationDetails.configuration.copy(
                hue = selectedColorInt.colorHue,
                brightness = LIGHT_CONFIGURATION_MAX_BRIGHTNESS.toInt(),
                saturation = selectedColorSaturation
            )
        )

        updateLight(
            updateLightDetails.id,
            updateLightDetails.configuration,
            updateLightDetails.status
        )
    }
}