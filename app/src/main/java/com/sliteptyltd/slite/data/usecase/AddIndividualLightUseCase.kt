package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.light.Light
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus

class AddIndividualLightUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(lightId: Int, address: String?, lightConfiguration: LightConfiguration, status: LightStatus): List<Light> =
        lightsRepository.addIndividualLight(
            Light(
                lightId,
                lightConfiguration,
                status,
                address
            )
        )
}