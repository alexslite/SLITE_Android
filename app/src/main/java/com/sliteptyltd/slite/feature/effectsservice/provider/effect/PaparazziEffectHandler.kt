package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.graphics.Color
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.PAPARAZZI
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.effectBlackoutColor
import com.sliteptyltd.slite.utils.Constants.Effects.daylightWhite
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturationHSV
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlin.random.Random

class PaparazziEffectHandler : LightEffectHandler {

    override val effect: Effect = PAPARAZZI
    private var effectJob: Job? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) = coroutineScope {
        effectJob = coroutineContext.job

        while (isActive) {
            val flashesCount = Random.nextInt(PAPARAZZI_FLASHES_COUNT_MIN_VALUE, PAPARAZZI_FLASHES_COUNT_MAX_VALUE)
            val cycleDelay = Random.nextLong(PAPARAZZI_CYCLE_DELAY_MIN_VALUE, PAPARAZZI_CYCLE_DELAY_MAX_VALUE)

            (PAPARAZZI_FLASHES_COUNT_MIN_VALUE..flashesCount).forEach { _ ->
                val flashBrightness = Random.nextInt(PAPARAZZI_BRIGHTNESS_MIN_VALUE, PAPARAZZI_BRIGHTNESS_MAX_VALUE)
                colorChangeCallback(
                    Color.HSVToColor(
                        floatArrayOf(
                            daylightWhite.colorHue,
                            daylightWhite.colorSaturationHSV,
                            flashBrightness.normalizeIn()
                        )
                    )
                )
                delay(PAPARAZZI_FLASH_DELAY)
            }
            colorChangeCallback(effectBlackoutColor)

            delay(cycleDelay)
        }
    }

    override fun stop() {
        effectJob?.cancel()
    }

    companion object {
        private const val PAPARAZZI_FLASHES_COUNT_MIN_VALUE = 1
        private const val PAPARAZZI_FLASHES_COUNT_MAX_VALUE = 4

        private const val PAPARAZZI_CYCLE_DELAY_MIN_VALUE = 75L
        private const val PAPARAZZI_CYCLE_DELAY_MAX_VALUE = 1001L
        private const val PAPARAZZI_FLASH_DELAY = 75L

        private const val PAPARAZZI_BRIGHTNESS_MIN_VALUE = 50
        private const val PAPARAZZI_BRIGHTNESS_MAX_VALUE = 101
    }
}