package com.sliteptyltd.slite.utils

import com.google.android.material.slider.Slider

open class OnSliderTouchListener : Slider.OnSliderTouchListener {
    override fun onStartTrackingTouch(slider: Slider) = Unit

    override fun onStopTrackingTouch(slider: Slider) = Unit
}