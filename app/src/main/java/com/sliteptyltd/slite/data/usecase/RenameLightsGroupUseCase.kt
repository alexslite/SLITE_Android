package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.light.Light

class RenameLightsGroupUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(groupId: Int, newGroupName: String): List<Light> {
        val light = lightsRepository.getLightsGroupById(groupId)?.copy()

        val lightsGroups = if (light != null) {
            val updatedLight = light.copy(lightConfiguration = light.lightConfiguration.copy(name = newGroupName))
            lightsRepository.updateLightsGroup(updatedLight)
        } else {
            lightsRepository.getLightsGroups()
        }

        return lightsGroups
    }
}