package com.sliteptyltd.slite.utils.extensions.views

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import com.sliteptyltd.slite.utils.Constants.Lights.DROPDOWN_ROTATION_DURATION

typealias OnAnimationStepAction = () -> Unit

fun View.animateInvisible(
    isInvisible: Boolean,
    duration: Long,
    interpolator: Interpolator = LinearInterpolator(),
    onAnimationEnd: OnAnimationStepAction? = null
) {
    if (this.isInvisible == isInvisible) return
    val startAlpha = if (isInvisible) 1f else 0f
    val endAlpha = if (isInvisible) 0f else 1f
    ValueAnimator.ofFloat(startAlpha, endAlpha).apply {
        this.interpolator = interpolator
        this.duration = duration
        addUpdateListener {
            this@animateInvisible.alpha = it.animatedValue as Float
        }
        doOnStart {
            if (!isInvisible) this@animateInvisible.isInvisible = isInvisible
        }
        doOnEnd {
            onAnimationEnd?.invoke()
            if (isInvisible) this@animateInvisible.isInvisible = isInvisible
        }
        start()
    }
}

fun View.animateHeightChange(
    startHeight: Int,
    endHeight: Int,
    duration: Long,
    interpolator: Interpolator = LinearInterpolator(),
    onAnimationStart: OnAnimationStepAction? = null,
    onAnimationEnd: OnAnimationStepAction? = null
) {
    ValueAnimator.ofInt(startHeight, endHeight).apply {
        this.duration = duration
        this.interpolator = interpolator

        addUpdateListener {
            updateLayoutParamsHeight(it.animatedValue as Int)
        }
        doOnStart { onAnimationStart?.invoke() }
        doOnEnd { onAnimationEnd?.invoke() }

        start()
    }
}

fun View.updateLayoutParamsHeight(height: Int) {
    updateLayoutParams {
        this.height = height
    }
}

fun View.rotateDropdownButton(endRotation: Float, duration: Long = DROPDOWN_ROTATION_DURATION, onRotationEndAction: (() -> Unit)? = null) {
    val currentRotation = this.rotation

    ValueAnimator.ofFloat(currentRotation, endRotation).apply {
        this.duration = duration
        addUpdateListener {
            this@rotateDropdownButton.rotation = it.animatedValue as Float
        }
        doOnEnd {
            onRotationEndAction?.invoke()
        }
        start()
    }
}