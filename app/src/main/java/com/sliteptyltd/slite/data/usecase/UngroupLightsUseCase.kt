package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository

class UngroupLightsUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(groupId: Int): Boolean = lightsRepository.ungroupLights(groupId)
}