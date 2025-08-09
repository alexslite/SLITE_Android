package com.sliteptyltd.slite.feature.effectsservice.data

import com.sliteptyltd.slite.feature.effectsservice.provider.effect.LightEffectHandler

data class EffectJobDetails(
    val lightsAddresses: MutableList<String>,
    val lightEffectHandler: LightEffectHandler
)