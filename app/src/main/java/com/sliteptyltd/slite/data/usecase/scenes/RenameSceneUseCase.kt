package com.sliteptyltd.slite.data.usecase.scenes

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.scene.Scene

class RenameSceneUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(sceneId: Int, newSceneName: String): List<Scene> =
        lightsRepository.renameScene(sceneId, newSceneName)
}