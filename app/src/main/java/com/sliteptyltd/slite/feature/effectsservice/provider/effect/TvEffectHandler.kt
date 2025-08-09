package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.graphics.Color
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.TV
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_HUE_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_SATURATION_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_VALUE_INDEX
import com.sliteptyltd.slite.utils.color.getEffectPhaseDiceRoll
import com.sliteptyltd.slite.utils.color.getRandomHue
import com.sliteptyltd.slite.utils.color.getRandomSaturation
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job

class TvEffectHandler : LightEffectHandler {

    override val effect: Effect = TV
    private var effectJob: Job? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) = coroutineScope {
        effectJob = coroutineContext.job
        var currentReferenceBrightness = TV_DEFAULT_BRIGHTNESS_VALUE
        val tvFrameColorArray = floatArrayOf(getRandomHue(), getRandomSaturation().normalizeIn(), currentReferenceBrightness.normalizeIn())

        while (isActive) {
            val tvEffectPhaseTypeChance = getEffectPhaseDiceRoll()

            if (tvEffectPhaseTypeChance > TV_COLOR_CHANGE_CHANCE_THRESHOLD) {
                tvFrameColorArray[HSV_ARRAY_HUE_INDEX] = getRandomHue()
                tvFrameColorArray[HSV_ARRAY_SATURATION_INDEX] =
                    Random.nextInt(TV_SATURATION_MIN_VALUE, TV_SATURATION_MAX_VALUE).normalizeIn()

                currentReferenceBrightness = Random.nextInt(TV_BRIGHTNESS_MIN_VALUE, TV_BRIGHTNESS_MAX_VALUE)
                tvFrameColorArray[HSV_ARRAY_VALUE_INDEX] = currentReferenceBrightness.normalizeIn()
            } else {
                val brightnessOffset = Random.nextInt(TV_BRIGHTNESS_OFFSET_MIN_VALUE, TV_BRIGHTNESS_OFFSET_MAX_VALUE)
                tvFrameColorArray[HSV_ARRAY_VALUE_INDEX] = (currentReferenceBrightness + brightnessOffset).normalizeIn()
            }

            colorChangeCallback(Color.HSVToColor(tvFrameColorArray))

            delay(Random.nextLong(TV_STROBE_DURATION_MIN_VALUE, TV_STROBE_DURATION_MAX_VALUE))
        }
    }

    override fun stop() {
        effectJob?.cancel()
    }

    companion object {
        private const val TV_STROBE_DURATION_MIN_VALUE = 150L
        private const val TV_STROBE_DURATION_MAX_VALUE = 1101L
        private const val TV_COLOR_CHANGE_CHANCE_THRESHOLD = 90

        private const val TV_SATURATION_MIN_VALUE = 0
        private const val TV_SATURATION_MAX_VALUE = 31

        private const val TV_BRIGHTNESS_MIN_VALUE = 30
        private const val TV_BRIGHTNESS_MAX_VALUE = 101

        private const val TV_DEFAULT_BRIGHTNESS_VALUE = 85
        private const val TV_BRIGHTNESS_OFFSET_MIN_VALUE = -15
        private const val TV_BRIGHTNESS_OFFSET_MAX_VALUE = 16
    }
}