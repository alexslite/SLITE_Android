package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.light.Light
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus

class UpdateLightUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(lightId: Int, configuration: LightConfiguration, status: LightStatus): Boolean =
        lightsRepository.updateLight(lightId, configuration.copy(), status)
}