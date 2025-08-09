package com.sliteptyltd.slite.feature.lightconfiguration.photosensitivity

import androidx.lifecycle.ViewModel
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.usecase.UpdateLightUseCase

class PhotoSensitivityViewModel(
    private val updateLight: UpdateLightUseCase
) : ViewModel() {

    fun setLightEffectsMode(lightId: Int, lightConfiguration: LightConfiguration) {
        updateLight(
            lightId,
            lightConfiguration.copy(configurationMode = EFFECTS),
            ON
        )
    }
}