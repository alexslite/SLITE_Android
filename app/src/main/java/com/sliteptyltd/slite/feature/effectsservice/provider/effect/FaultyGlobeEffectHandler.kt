package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.graphics.Color
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.FAULTY_GLOBE
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_VALUE_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MAX_VALUE
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_MIN_VALUE
import com.sliteptyltd.slite.utils.Constants.Effects.daylightWhite
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturationHSV
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.supervisorScope

class FaultyGlobeEffectHandler : LightEffectHandler {

    override val effect: Effect = FAULTY_GLOBE
    private var effectJob: Job? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) = supervisorScope {
        effectJob = coroutineContext.job

        val globeColorArray = floatArrayOf(daylightWhite.colorHue, daylightWhite.colorSaturationHSV, HSV_MIN_VALUE)

        while (isActive) {
            val workingStateDuration = Random.nextLong(FAULTY_WORKING_STATE_DURATION_MIN_VALUE, FAULTY_WORKING_STATE_DURATION_MAX_VALUE)
            globeColorArray[HSV_ARRAY_VALUE_INDEX] = HSV_MAX_VALUE
            colorChangeCallback(Color.HSVToColor(globeColorArray))
            delay(workingStateDuration)

            val flickerDelay = Random.nextLong(FAULTY_GLOBE_DIM_DURATION_MIN_VALUE, FAULTY_GLOBE_DIM_DURATION_MAX_VALUE)
            val outputBrightness = Random.nextInt(FAULTY_GLOBE_BRIGHTNESS_MIN_VALUE, FAULTY_GLOBE_BRIGHTNESS_MAX_VALUE)
            globeColorArray[HSV_ARRAY_VALUE_INDEX] = outputBrightness.normalizeIn()
            colorChangeCallback(Color.HSVToColor(globeColorArray))

            delay(flickerDelay)
        }
    }

    override fun stop() {
        effectJob?.cancel()
    }

    companion object {
        private const val FAULTY_WORKING_STATE_DURATION_MIN_VALUE = 0L
        private const val FAULTY_WORKING_STATE_DURATION_MAX_VALUE = 1501L

        private const val FAULTY_GLOBE_DIM_DURATION_MIN_VALUE = 0L
        private const val FAULTY_GLOBE_DIM_DURATION_MAX_VALUE = 501L

        private const val FAULTY_GLOBE_BRIGHTNESS_MIN_VALUE = 50
        private const val FAULTY_GLOBE_BRIGHTNESS_MAX_VALUE = 101
    }
}