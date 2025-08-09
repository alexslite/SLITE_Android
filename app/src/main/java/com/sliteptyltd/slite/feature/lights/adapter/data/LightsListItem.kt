package com.sliteptyltd.slite.feature.lights.adapter.data

import androidx.annotation.FloatRange
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_BATTERY_MAX_PERCENTAGE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_BATTERY_MIN_PERCENTAGE

sealed class LightsListItem(open val itemId: Int) {

    data class SectionHeader(
        override val itemId: Int,
        val type: LightsSectionType,
        val sectionSize: Int,
        val isExpanded: Boolean = true
    ) : LightsListItem(itemId)

    data class LightItem(
        override val itemId: Int,
        val lightConfiguration: LightConfiguration,
        val status: LightStatus,
        val lightUIConfiguration: LightUIConfiguration = LightUIConfiguration(),
        @FloatRange(from = LIGHT_BATTERY_MIN_PERCENTAGE, to = LIGHT_BATTERY_MAX_PERCENTAGE)
        val batteryPercentage: Float? = null
    ) : LightsListItem(itemId)

    data class LightsSectionControlItem(
        override val itemId: Int,
        val type: LightsSectionType
    ) : LightsListItem(itemId)
}