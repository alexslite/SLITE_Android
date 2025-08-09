package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.graphics.Color
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.DISCO
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_HUE_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_SATURATION_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_VALUE
import com.sliteptyltd.slite.utils.color.getRandomHue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.supervisorScope

class DiscoEffectHandler : LightEffectHandler {

    override val effect: Effect = DISCO
    private var effectJob: Job? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) = supervisorScope {
        effectJob = coroutineContext.job

        val discoColorArray = floatArrayOf(getRandomHue(), HSV_MAX_VALUE, HSV_MAX_VALUE)

        while (isActive) {
            discoColorArray[HSV_ARRAY_HUE_INDEX] = getRandomHue()
            discoColorArray[HSV_ARRAY_SATURATION_INDEX] = HSV_MAX_VALUE
            colorChangeCallback(Color.HSVToColor(discoColorArray))

            delay(DISCO_COLOR_HOLD_DURATION)
        }
    }

    override fun stop() {
        effectJob?.cancel()
    }

    companion object {
        private const val DISCO_COLOR_HOLD_DURATION = 600L
    }
}