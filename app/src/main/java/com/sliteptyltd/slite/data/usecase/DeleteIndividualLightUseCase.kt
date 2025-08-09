package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.light.Light

class DeleteIndividualLightUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(lightId: Int): List<Light> =
        lightsRepository.deleteIndividualLight(lightId)
}