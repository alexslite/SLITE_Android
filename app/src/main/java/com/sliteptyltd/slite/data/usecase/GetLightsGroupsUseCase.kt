package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.light.Light

class GetLightsGroupsUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(): List<Light> = lightsRepository.getLightsGroups()
}