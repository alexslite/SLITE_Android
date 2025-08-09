package com.sliteptyltd.slite.data.model.scene

import com.sliteptyltd.slite.data.model.light.LightConfiguration

data class SceneLightDetails(
    val lightId: Int,
    val configuration: LightConfiguration,
    val address: String?,
    val groupId: Int
)