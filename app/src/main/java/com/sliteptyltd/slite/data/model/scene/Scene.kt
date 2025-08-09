package com.sliteptyltd.slite.data.model.scene

import org.joda.time.DateTime

data class Scene(
    val id: Int,
    val name: String,
    val lightsConfigurations: List<SceneLightDetails>,
    val createdAt: DateTime = DateTime.now()
)