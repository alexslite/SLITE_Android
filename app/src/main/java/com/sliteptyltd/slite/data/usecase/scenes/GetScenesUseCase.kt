package com.sliteptyltd.slite.data.usecase.scenes

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.scene.Scene

class GetScenesUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(): List<Scene> = lightsRepository.getScenes()
}