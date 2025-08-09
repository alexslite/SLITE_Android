package com.sliteptyltd.slite.utils.extensions

/**
 * Normalizes a value, meaning that the @return will be a value between
 * [0,1], IF the [this] is in [minValue], [maxValue]
 * This value is proportional with the interval [[minValue], [maxValue]].
 *
 * e.g.: if we give the interval [0, 50] and the number 20, after normalizing the value
 * it will be 0.4.
 */

fun Int.normalizeIn(minValue: Float = 0f, maxValue: Float = 100f): Float =
    ((this.toFloat() - minValue) / (maxValue - minValue)).coerceIn(minValue, maxValue)

fun Float.normalizeIn(minValue: Float = 0f, maxValue: Float = 100f): Float =
    ((this - minValue) / (maxValue - minValue)).coerceIn(minValue, maxValue)