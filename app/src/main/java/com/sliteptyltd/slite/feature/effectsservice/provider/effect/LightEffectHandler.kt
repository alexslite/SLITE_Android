package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback

interface LightEffectHandler {

    val effect: Effect

    suspend fun start(colorChangeCallback: OnColorChangedCallback)

    fun stop()
}