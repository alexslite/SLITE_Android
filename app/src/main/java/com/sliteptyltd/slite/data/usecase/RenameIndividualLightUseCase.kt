package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.light.Light

class RenameIndividualLightUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(lightId: Int, newLightName: String): List<Light> {
        val light = lightsRepository.getIndividualLightById(lightId)?.copy()

        val individualLights = if (light != null) {
            val updatedLight = light.copy(lightConfiguration = light.lightConfiguration.copy(name = newLightName))
            lightsRepository.updateIndividualLight(updatedLight)
        } else {
            lightsRepository.getIndividualLights()
        }

        return individualLights
    }
}