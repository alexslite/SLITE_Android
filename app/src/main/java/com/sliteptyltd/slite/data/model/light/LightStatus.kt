package com.sliteptyltd.slite.data.model.light

import android.content.res.Resources
import androidx.annotation.Keep
import com.sliteptyltd.slite.R

@Keep
enum class LightStatus {
    ON, OFF, DISCONNECTED;

    fun getSingleLightStatusText(resources: Resources): String =
        when (this) {
            ON -> resources.getString(R.string.lights_list_light_item_status_on_text)
            OFF -> resources.getString(R.string.lights_list_light_item_status_off_text)
            DISCONNECTED -> resources.getString(R.string.lights_list_light_item_status_disconnected_text)
        }

    fun getConnectedLightsStatus(connectedLightsCount: Int, poweredOnLightsCount: Int, resources: Resources): String? =
        when (this) {
            ON -> resources.getString(R.string.lights_list_group_connected_lights_status_on_text, poweredOnLightsCount, connectedLightsCount)
            OFF -> resources.getString(R.string.lights_list_group_connected_lights_status_off_text, connectedLightsCount)
            else -> null
        }
}