package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository

class CreateGroupUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(groupName: String, groupedLightsIds: List<Int>): Int = lightsRepository.createGroup(groupName, groupedLightsIds)
}