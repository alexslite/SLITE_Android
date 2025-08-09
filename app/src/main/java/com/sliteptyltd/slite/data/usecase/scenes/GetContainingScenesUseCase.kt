package com.sliteptyltd.slite.data.usecase.scenes

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.scene.Scene

class GetContainingScenesUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(lightId: Int): List<Scene> = lightsRepository.getScenesContainingLight(lightId)
}