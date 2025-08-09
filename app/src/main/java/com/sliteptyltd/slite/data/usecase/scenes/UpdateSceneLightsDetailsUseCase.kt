package com.sliteptyltd.slite.data.usecase.scenes

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.scene.SceneLightDetails

class UpdateSceneLightsDetailsUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(sceneLightsDetails: List<SceneLightDetails>) = lightsRepository.updateSceneLightsDetails(sceneLightsDetails)
}