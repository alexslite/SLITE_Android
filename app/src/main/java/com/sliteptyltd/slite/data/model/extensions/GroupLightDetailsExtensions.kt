package com.sliteptyltd.slite.data.model.extensions

import android.content.res.Resources
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.light.GroupLightDetails
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.utils.Constants.Lights.GROUP_LIGHTS_MULTIPLE_STATUSES_FORMAT

fun GroupLightDetails.updatePoweredOnStatus(newStatus: LightStatus): GroupLightDetails =
    if (status != DISCONNECTED) GroupLightDetails(lightId, name, newStatus, address, mode) else this

fun List<GroupLightDetails>.getLightsGroupStatusText(resources: Resources): String {
    val disconnectedLightsCount = count { it.status == DISCONNECTED }
    val disconnectedLightsStatusText = if (disconnectedLightsCount != 0) {
        resources.getString(R.string.lights_list_group_status_disconnected_text, disconnectedLightsCount)
    } else {
        null
    }

    val connectedLightsCount = size - disconnectedLightsCount
    val poweredOnLightsCount = count { it.status == ON }
    val connectedLightsStatusText = getGroupStatus().getConnectedLightsStatus(connectedLightsCount, poweredOnLightsCount, resources)

    return when {
        !connectedLightsStatusText.isNullOrEmpty() && !disconnectedLightsStatusText.isNullOrEmpty() ->
            String.format(GROUP_LIGHTS_MULTIPLE_STATUSES_FORMAT, connectedLightsStatusText, disconnectedLightsStatusText)
        !connectedLightsStatusText.isNullOrEmpty() -> connectedLightsStatusText
        !disconnectedLightsStatusText.isNullOrEmpty() -> disconnectedLightsStatusText
        else -> ""
    }
}

fun List<GroupLightDetails>.getGroupStatus(): LightStatus =
    when {
        all { it.status == DISCONNECTED } -> DISCONNECTED
        any { it.status == ON } -> ON
        else -> OFF
    }