package com.sliteptyltd.slite.data.usecase.scenes

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.scene.Scene

class CreateSceneUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(sceneName: String): List<Scene> = lightsRepository.createScene(sceneName)
}