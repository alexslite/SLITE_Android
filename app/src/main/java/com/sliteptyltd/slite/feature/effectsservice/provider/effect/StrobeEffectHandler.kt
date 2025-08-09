package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.STROBE
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.effectBlackoutColor
import com.sliteptyltd.slite.utils.Constants.Effects.daylightWhite
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

class StrobeEffectHandler : LightEffectHandler {

    override val effect: Effect = STROBE
    private var effectJob: Job? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) = coroutineScope {
        effectJob = coroutineContext.job

        var isStrobingLightOn = true
        var currentColor: Int
        while (isActive) {
            currentColor = if (isStrobingLightOn) {
                daylightWhite
            } else {
                effectBlackoutColor
            }
            delay(STROBE_PHASE_DURATION)
            colorChangeCallback(currentColor)
            isStrobingLightOn = !isStrobingLightOn
        }
    }

    override fun stop() {
        effectJob?.cancel()
    }

    companion object {
        private const val STROBE_PHASE_DURATION = 75L
    }
}