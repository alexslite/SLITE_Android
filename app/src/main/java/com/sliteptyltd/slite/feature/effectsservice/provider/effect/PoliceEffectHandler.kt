package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.graphics.Color
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.POLICE
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.effectBlackoutColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

class PoliceEffectHandler : LightEffectHandler {

    override val effect: Effect = POLICE
    private var effectJob: Job? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) = coroutineScope {
        effectJob = coroutineContext.job

        while (isActive) {
            // Red light cycle
            colorChangeCallback(POLICE_RED_STROBE)
            delay(POLICE_COLOR_STROBE_DURATION)

            colorChangeCallback(effectBlackoutColor)
            delay(POLICE_COLOR_STROBE_DURATION)

            colorChangeCallback(POLICE_RED_STROBE)
            delay(POLICE_COLOR_STROBE_DURATION)

            colorChangeCallback(effectBlackoutColor)
            delay(POLICE_COLOR_STROBE_DURATION)

            // Blue light cycle
            colorChangeCallback(POLICE_BLUE_STROBE)
            delay(POLICE_COLOR_STROBE_DURATION)

            colorChangeCallback(effectBlackoutColor)
            delay(POLICE_COLOR_STROBE_DURATION)

            colorChangeCallback(POLICE_BLUE_STROBE)
            delay(POLICE_COLOR_STROBE_DURATION)

            colorChangeCallback(effectBlackoutColor)
            delay(POLICE_COLOR_STROBE_DURATION)
        }
    }

    override fun stop() {
        effectJob?.cancel()
    }

    companion object {
        private const val POLICE_COLOR_STROBE_DURATION = 100L
        private val POLICE_RED_STROBE = Color.parseColor("#FF000C")
        private val POLICE_BLUE_STROBE = Color.parseColor("#0C00FF")
    }
}