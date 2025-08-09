package com.sliteptyltd.slite.data.usecase.scenes

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.scene.Scene

class DeleteSceneUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(sceneId: Int): List<Scene> = lightsRepository.deleteScene(sceneId)
}