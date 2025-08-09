package com.sliteptyltd.slite.feature.effectsservice.provider.effect

import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnRepeat
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.FIREWORKS
import com.sliteptyltd.slite.feature.effectsservice.provider.OnColorChangedCallback
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_HUE_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_SATURATION_INDEX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.HSV_ARRAY_VALUE_INDEX
import com.sliteptyltd.slite.utils.Constants.Effects.EFFECT_MIN_REPEAT_COUNT
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturation
import com.sliteptyltd.slite.utils.color.getEffectPhaseDiceRoll
import com.sliteptyltd.slite.utils.extensions.normalizeIn
import java.lang.Integer.max
import kotlin.random.Random

class FireworksEffectHandler : LightEffectHandler {

    override val effect: Effect = FIREWORKS
    private val fireworksAnimator: ValueAnimator = initAnimator()

    override suspend fun start(colorChangeCallback: OnColorChangedCallback) {
        var fireworksCount = getFireworksCount()
        var fireworksColors = getFireworksCycleColors(fireworksCount)
        var fireworksBrightnessValues = getFireworksCycleBrightnessValues(fireworksCount)
        var fireworksDurations: List<Long> = if (fireworksCount == 1) listOf() else getFireworksCycleShortDurations(fireworksCount)
        var currentIndex = if (fireworksCount == 1) 0 else 1
        var currentFireworkColor = fireworksColors.first()
        var currentFireworkBrightness = fireworksBrightnessValues.first()
        val fireworkColorArray =
            floatArrayOf(
                currentFireworkColor.colorHue,
                currentFireworkColor.colorSaturation.normalizeIn(),
                currentFireworkBrightness.normalizeIn()
            )
        var startBrightness = fireworksBrightnessValues.first()
        var endBrightness = if (fireworksCount == 1) 0 else max(FIREWORKS_BRIGHTNESS_MIN_VALUE, startBrightness - 25)
        fireworksAnimator.setIntValues(startBrightness, endBrightness)
        fireworksAnimator.duration =
            if (fireworksCount == 1) getFireworksSequenceDuration() else fireworksDurations.first()
        fireworksAnimator.repeatCount = EFFECT_MIN_REPEAT_COUNT

        fireworksAnimator.addUpdateListener {
            fireworkColorArray[HSV_ARRAY_HUE_INDEX] = currentFireworkColor.colorHue
            fireworkColorArray[HSV_ARRAY_SATURATION_INDEX] = currentFireworkColor.colorSaturation.normalizeIn()
            fireworkColorArray[HSV_ARRAY_VALUE_INDEX] = (it.animatedValue as Int).normalizeIn()
            colorChangeCallback(Color.HSVToColor(fireworkColorArray))
        }

        fireworksAnimator.doOnRepeat {
            fireworksAnimator.cancel()

            if (currentIndex == 0) {
                fireworksAnimator.startDelay = getFireworkDelay()
                fireworksCount = getFireworksCount()
                fireworksColors = getFireworksCycleColors(fireworksCount)
                fireworksBrightnessValues = getFireworksCycleBrightnessValues(fireworksCount)
                fireworksDurations = if (fireworksCount == 1) listOf() else getFireworksCycleShortDurations(fireworksCount)
            }

            when {
                currentIndex < fireworksColors.lastIndex -> {
                    startBrightness = fireworksBrightnessValues[currentIndex]
                    endBrightness = max(FIREWORKS_BRIGHTNESS_MIN_VALUE, startBrightness - 25)
                    fireworksAnimator.setIntValues(startBrightness, endBrightness)
                    fireworksAnimator.duration = fireworksDurations[currentIndex]

                    currentFireworkColor = fireworksColors[currentIndex]
                    currentFireworkBrightness = fireworksBrightnessValues[currentIndex]

                    currentIndex++
                }
                currentIndex == fireworksColors.lastIndex -> {
                    startBrightness = fireworksBrightnessValues[currentIndex]
                    endBrightness = 0
                    fireworksAnimator.setIntValues(startBrightness, endBrightness)
                    fireworksAnimator.duration = Random.nextLong(fireworksDurations.sum(), FIREWORKS_DURATION_MAX_VALUE)

                    currentFireworkColor = fireworksColors[currentIndex]
                    currentFireworkBrightness = fireworksBrightnessValues[currentIndex]

                    currentIndex = 0
                }
            }

            fireworksAnimator.start()
        }

        fireworksAnimator.start()
    }

    override fun stop() {
        fireworksAnimator.cancel()
    }

    private fun initAnimator(): ValueAnimator =
        ValueAnimator().apply {
            interpolator = DecelerateInterpolator()
        }

    private fun getFireworksCycleShortDurations(cycleFireworksCount: Int): List<Long> {
        val durationsList = mutableListOf<Long>()
        (1 until cycleFireworksCount).forEach { _ ->
            durationsList.add(getShortFireworkDuration())
        }
        return durationsList
    }

    private fun getFireworksCycleColors(cycleFireworksCount: Int): List<Int> {
        val colorsList = mutableListOf<Int>()
        (1..cycleFireworksCount).forEach { _ ->
            colorsList.add(getFireworkColor())
        }
        return colorsList
    }

    private fun getFireworksCycleBrightnessValues(cycleFireworksCount: Int): List<Int> {
        val brightnessValues = mutableListOf<Int>()
        (1..cycleFireworksCount).forEach { _ ->
            brightnessValues.add(getFireworkBrightness())
        }
        return brightnessValues
    }

    private fun getFireworksCount(): Int {
        val fireworksCountDiceRoll = getEffectPhaseDiceRoll()
        return when {
            fireworksCountDiceRoll <= FIREWORKS_COUNT_ONE_THRESHOLD -> 1
            fireworksCountDiceRoll <= FIREWORKS_COUNT_TWO_THRESHOLD -> 2
            fireworksCountDiceRoll <= FIREWORKS_COUNT_THREE_THRESHOLD -> 3
            fireworksCountDiceRoll <= FIREWORKS_COUNT_FOUR_THRESHOLD -> 4
            else -> 5
        }
    }

    private fun getFireworkColor(): Int {
        val fireworkColorDiceRoll = getEffectPhaseDiceRoll()

        val fireworkColorIndex = when {
            fireworkColorDiceRoll <= RED_THRESHOLD -> INDEX_RED
            fireworkColorDiceRoll <= BLUE_THRESHOLD -> INDEX_BLUE
            fireworkColorDiceRoll <= GREEN_THRESHOLD -> INDEX_GREEN
            fireworkColorDiceRoll <= GOLD_THRESHOLD -> INDEX_GOLD
            fireworkColorDiceRoll <= ORANGE_THRESHOLD -> INDEX_ORANGE
            fireworkColorDiceRoll <= PURPLE_THRESHOLD -> INDEX_PURPLE
//            fireworkColorDiceRoll <= SILVER_THRESHOLD -> INDEX_SILVER
            else -> INDEX_YELLOW
        }

        return fireworksColors[fireworkColorIndex]
    }

    private fun getFireworkDelay(): Long = Random.nextLong(FIREWORKS_DELAY_MIN_VALUE, FIREWORKS_DELAY_MAX_VALUE)

    private fun getFireworkBrightness(): Int = Random.nextInt(FIREWORKS_BRIGHTNESS_MIN_VALUE, FIREWORKS_BRIGHTNESS_MAX_VALUE)

    private fun getShortFireworkDuration(): Long = Random.nextLong(100, 500)

    private fun getFireworksSequenceDuration(): Long = Random.nextLong(FIREWORKS_DURATION_MIN_VALUE, FIREWORKS_DURATION_MAX_VALUE)

    companion object {
        private const val FIREWORKS_DELAY_MIN_VALUE = 0L
        private const val FIREWORKS_DELAY_MAX_VALUE = 1601L

        private const val FIREWORKS_BRIGHTNESS_MIN_VALUE = 30
        private const val FIREWORKS_BRIGHTNESS_MAX_VALUE = 101

        private const val FIREWORKS_DURATION_MIN_VALUE = 1000L
        private const val FIREWORKS_DURATION_MAX_VALUE = 3001L

        private const val FIREWORKS_COUNT_ONE_THRESHOLD = 50
        private const val FIREWORKS_COUNT_TWO_THRESHOLD = 75
        private const val FIREWORKS_COUNT_THREE_THRESHOLD = 87
        private const val FIREWORKS_COUNT_FOUR_THRESHOLD = 95

        private const val RED_THRESHOLD = 40
        private const val BLUE_THRESHOLD = 80
        private const val GREEN_THRESHOLD = 85
        private const val GOLD_THRESHOLD = 90
        private const val ORANGE_THRESHOLD = 93
        private const val PURPLE_THRESHOLD = 96
//        private const val SILVER_THRESHOLD = 98

        private const val INDEX_RED = 0
        private const val INDEX_BLUE = 1
        private const val INDEX_GREEN = 2
        private const val INDEX_GOLD = 3
        private const val INDEX_ORANGE = 4
        private const val INDEX_PURPLE = 5
//        private const val INDEX_SILVER = 6
        private const val INDEX_YELLOW = 6

        private val fireworksColors = listOf(
            Color.parseColor("#FF0000"), // RED
            Color.parseColor("#0000FF"), // BLUE
            Color.parseColor("#00FF00"), // GREEN
            Color.parseColor("#F2CD82"), // GOLD
            Color.parseColor("#FF7F00"), // ORANGE
            Color.parseColor("#FF00FF"), // PURPLE
//            Color.parseColor("#CCCCCC"), // SILVER
            Color.parseColor("#FFFF00")  // YELLOW
        )
    }
}