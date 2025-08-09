package com.sliteptyltd.slite.feature.effectsservice

import androidx.annotation.ColorInt

interface EffectsPlayer {

    fun setColor(lightAddress: String, @ColorInt color: Int)
}