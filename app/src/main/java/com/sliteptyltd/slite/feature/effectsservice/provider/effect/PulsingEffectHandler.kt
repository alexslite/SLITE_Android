package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.RESTART
import android.graphics.Color
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.PULSING
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_SATURATION
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_HUE_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MIN_HUE_VALUE

class PulsingEffectHandler : LightEffectHandler {

    override val effect: Effect = PULSING
    private val pulseAnimator: ValueAnimator = initAnimator()

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) {
        pulseAnimator.addUpdateListener {
            colorChangeCallback(Color.HSVToColor(floatArrayOf((it.animatedValue as Int).toFloat(), HSV_MAX_SATURATION, HSV_MAX_VALUE)))
        }

        pulseAnimator.start()
    }

    override fun stop() {
        pulseAnimator.cancel()
    }

    private fun initAnimator(): ValueAnimator =
        ValueAnimator.ofInt(MIN_HUE_VALUE.toInt(), MAX_HUE_VALUE.toInt()).apply {
            duration = PULSING_DURATION
            repeatCount = INFINITE
            repeatMode = RESTART
        }

    companion object {
        private const val PULSING_DURATION = 5500L
    }
}