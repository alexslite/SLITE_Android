package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.graphics.Color
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.LIGHTNING
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.effectBlackoutColor
import com.sliteptyltd.slite.utils.Constants.Effects.daylightWhite
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturationHSV
import com.sliteptyltd.slite.utils.color.getEffectPhaseDiceRoll
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlin.random.Random

class LightningEffectHandler : LightEffectHandler {

    override val effect: Effect = LIGHTNING
    private var effectJob: Job? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) = coroutineScope {
        effectJob = coroutineContext.job

        while (isActive) {
            val flashesCountDiceRoll = getEffectPhaseDiceRoll()
            val flashesCount = if (flashesCountDiceRoll < FLASHES_COUNT_CHANCE_THRESHOLD) {
                Random.nextInt(LIGHTNING_FLASHES_COUNT_MIN_VALUE, FLASHES_COUNT_THRESHOLD)
            } else {
                Random.nextInt(FLASHES_COUNT_THRESHOLD, LIGHTNING_FLASHES_COUNT_MAX_VALUE)
            }

            (1..flashesCount).forEach { _ ->
                colorChangeCallback(
                    Color.HSVToColor(
                        floatArrayOf(
                            daylightWhite.colorHue,
                            daylightWhite.colorSaturationHSV,
                            Random.nextInt(LIGHTNING_MIN_BRIGHTNESS, LIGHTNING_MAX_BRIGHTNESS).normalizeIn()
                        )
                    )
                )
                delay(Random.nextLong(FLASH_DELAY_MIN_VALUE, FLASH_DELAY_MAM_VALUE))
            }
            colorChangeCallback(effectBlackoutColor)

            delay(Random.nextLong(LIGHTNING_CYCLE_PAUSE_MIN_VALUE, LIGHTNING_CYCLE_PAUSE_MAX_VALUE))
        }
    }

    override fun stop() {
        effectJob?.cancel()
    }

    companion object {
        private const val LIGHTNING_CYCLE_PAUSE_MIN_VALUE = 500L
        private const val LIGHTNING_CYCLE_PAUSE_MAX_VALUE = 5001L

        private const val LIGHTNING_MIN_BRIGHTNESS = 30
        private const val LIGHTNING_MAX_BRIGHTNESS = 100

        private const val LIGHTNING_FLASHES_COUNT_MIN_VALUE = 1
        private const val LIGHTNING_FLASHES_COUNT_MAX_VALUE = 10

        private const val FLASH_DELAY_MIN_VALUE = 50L
        private const val FLASH_DELAY_MAM_VALUE = 300L
        private const val FLASHES_COUNT_CHANCE_THRESHOLD = 50
        private const val FLASHES_COUNT_THRESHOLD = 4
    }
}