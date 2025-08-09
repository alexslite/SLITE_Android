package com.sliteptyltd.slite.utils.bindingadapters

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.utils.Constants.GRADIENT_MIN_COLOR_COUNT

@BindingAdapter("sceneBackgroundGradientColors")
fun View.setSceneBackgroundGradient(sceneColors: List<Int>) {
    background = if (sceneColors.isNotEmpty()) {
        val colorsArray = if (sceneColors.size >= GRADIENT_MIN_COLOR_COUNT) {
            sceneColors.toIntArray()
        } else {
            val lightColor = sceneColors.first()
            intArrayOf(lightColor, lightColor)
        }
        GradientDrawable(
            LEFT_RIGHT,
            colorsArray
        ).apply { cornerRadius = resources.getDimension(R.dimen.scenes_list_item_corner_radius) }
    } else {
        ResourcesCompat.getDrawable(resources, R.drawable.bg_default_scene_gradient, null)
    }
}