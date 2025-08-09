package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.FIRE
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.Effects.EFFECT_MIN_REPEAT_COUNT
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturationHSV
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import kotlin.random.Random

class FireEffectHandler : LightEffectHandler {

    override val effect: Effect = FIRE
    private var fireAnimator: ValueAnimator? = null
    private var fadeAnimator: ValueAnimator? = null

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) {
        var firstColor = getRandomFireplaceShade()
        var secondColor = getRandomFireplaceShade()

        fireAnimator = ObjectAnimator.ofObject(ArgbEvaluator(), firstColor, secondColor)
        fireAnimator?.interpolator = AccelerateInterpolator()

        fadeAnimator = ValueAnimator.ofInt(FIREPLACE_FADE_MAX_VALUE, FIREPLACE_FADE_MIN_VALUE, FIREPLACE_FADE_MAX_VALUE)
        fadeAnimator?.duration = FIREPLACE_FADE_DURATION_MS
        fadeAnimator?.addUpdateListener {
            val brightness = (it.animatedValue as Int).normalizeIn()
            val colorArray = floatArrayOf(firstColor.colorHue, firstColor.colorSaturationHSV, brightness)
            colorChangeCallback(Color.HSVToColor(colorArray))
        }

        fireAnimator?.addUpdateListener {
            colorChangeCallback(it.animatedValue as Int)
        }
        fadeAnimator?.doOnEnd {
            fireAnimator?.start()
        }

        fireAnimator?.duration = Random.nextLong(FIREPLACE_MIN_DURATION, FIREPLACE_MAX_DURATION)
        fireAnimator?.repeatCount = EFFECT_MIN_REPEAT_COUNT

        fireAnimator?.doOnRepeat {
            fireAnimator?.cancel()
            firstColor = secondColor
            while (secondColor == firstColor) {
                secondColor = getRandomFireplaceShade()
            }
            fireAnimator?.duration = Random.nextLong(FIREPLACE_MIN_DURATION, FIREPLACE_MAX_DURATION)
            fireAnimator?.setObjectValues(firstColor, secondColor)

            fadeAnimator?.start()
        }

        fireAnimator?.start()
    }

    override fun stop() {
        fadeAnimator?.cancel()
        fireAnimator?.cancel()
        fireAnimator = null
    }

    companion object {
        private const val FIREPLACE_COLOR_SHADES_START_INDEX = 0
        private const val FIREPLACE_MIN_DURATION = 600L
        private const val FIREPLACE_MAX_DURATION = 1001L
        private const val FIREPLACE_FADE_DURATION_MS = 500L
        private const val FIREPLACE_FADE_MAX_VALUE = 100
        private const val FIREPLACE_FADE_MIN_VALUE = 70

        private val fireplaceColorShades = listOf(
            Color.parseColor("#FB8C00"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#F57C00"),
            Color.parseColor("#E88504")
        )

        fun getRandomFireplaceShade(): Int =
            fireplaceColorShades[Random.nextInt(
                FIREPLACE_COLOR_SHADES_START_INDEX,
                fireplaceColorShades.size
            )]
    }
}