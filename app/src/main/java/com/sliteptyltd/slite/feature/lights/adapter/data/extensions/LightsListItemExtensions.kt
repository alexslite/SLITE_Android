package com.sliteptyltd.slite.feature.lights.adapter.data.extensions

import com.sliteptyltd.slite.data.model.extensions.updatePoweredOnStatus
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem

fun LightsListItem.isSingleLightItem(): Boolean =
    (this is LightsListItem.LightItem) && lightConfiguration.isIndividualLight

fun LightsListItem.LightItem.updateSingleLightPowerState(isPoweredOn: Boolean): LightsListItem.LightItem =
    if (isSingleLightItem() && status != DISCONNECTED) {
        val newStatus = if (isPoweredOn) ON else OFF
        copy(status = newStatus)
    } else {
        this
    }

fun LightsListItem.LightItem.isLightsGroupItem(): Boolean = lightConfiguration.isLightsGroup


fun LightsListItem.LightItem.updateLightGroupPowerState(isPoweredOn: Boolean): LightsListItem.LightItem =
    if (isLightsGroupItem() && status != DISCONNECTED) {
        val newStatus = if (isPoweredOn) ON else OFF
        copy(
            status = newStatus,
            lightConfiguration = lightConfiguration.copy(
                lightsDetails = lightConfiguration.lightsDetails.map { it.updatePoweredOnStatus(newStatus) }
            )
        )
    } else {
        this
    }