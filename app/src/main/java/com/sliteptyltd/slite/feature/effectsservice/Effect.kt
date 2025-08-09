package com.sliteptyltd.slite.feature.effectsservice

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.utils.Constants.Effects.defaultEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.defaultEffectGradientOrientation
import com.sliteptyltd.slite.utils.Constants.Effects.discoEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.faultyGlobeEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.fireEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.fireEffectGradientOrientation
import com.sliteptyltd.slite.utils.Constants.Effects.fireworksEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.fireworksEffectGradientOrientation
import com.sliteptyltd.slite.utils.Constants.Effects.lightningEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.paparazziEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.policeEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.pulsingEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.strobeEffectGradientColors
import com.sliteptyltd.slite.utils.Constants.Effects.tvEffectGradientColors
import kotlin.math.roundToInt

enum class Effect(
    val gradientColors: IntArray,
    val gradientOrientation: Orientation,
) {
    FAULTY_GLOBE(
        faultyGlobeEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    PAPARAZZI(
        paparazziEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    TV(
        tvEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    LIGHTNING(
        lightningEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    POLICE(
        policeEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    FIREWORKS(
        fireworksEffectGradientColors,
        fireworksEffectGradientOrientation
    ),
    DISCO(
        discoEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    PULSING(
        pulsingEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    STROBE(
        strobeEffectGradientColors,
        defaultEffectGradientOrientation
    ),
    FIRE(
        fireEffectGradientColors,
        fireEffectGradientOrientation
    );

    companion object {
        fun fromString(effect: String?): Effect? =
            effect?.let { valueOf(it) }

        fun Effect?.getGradientDrawable(resources: Resources): GradientDrawable =
            GradientDrawable(
                this?.gradientOrientation ?: defaultEffectGradientOrientation,
                this?.gradientColors ?: defaultEffectGradientColors
            ).apply {
                this.cornerRadius = resources.getDimension(R.dimen.lights_list_power_button_corners)
                val size = resources.getDimension(R.dimen.lights_list_power_button_size).roundToInt()
                this.setSize(size, size)
            }
    }
}