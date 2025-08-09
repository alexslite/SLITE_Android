package com.sliteptyltd.slite.data.usecase

import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.model.light.Light

class UpdateLightsSectionPowerStatusUseCase(
    private val lightsRepository: LightsRepository
) {

    operator fun invoke(isGroupsSection: Boolean, isSectionStateOn: Boolean): List<Light> =
        lightsRepository.updateLightsSectionPowerStatus(isGroupsSection, isSectionStateOn)
}