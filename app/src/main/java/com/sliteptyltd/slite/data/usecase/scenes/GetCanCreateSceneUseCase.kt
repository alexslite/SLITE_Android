package com.sliteptyltd.slite.data.usecase.scenes

import com.sliteptyltd.slite.data.LightsRepository

class GetCanCreateSceneUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(): Boolean = lightsRepository.getCanCreateScenes()
}