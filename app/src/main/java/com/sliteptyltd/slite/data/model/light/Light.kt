package com.sliteptyltd.slite.data.model.light

import androidx.annotation.FloatRange
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_BATTERY_MAX_PERCENTAGE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_BATTERY_MIN_PERCENTAGE

data class Light(
    val id: Int,
    val lightConfiguration: LightConfiguration,
    val status: LightStatus,
    val address: String?,
    @FloatRange(from = LIGHT_BATTERY_MIN_PERCENTAGE, to = LIGHT_BATTERY_MAX_PERCENTAGE)
    val batteryPercentage: Float? = null
)