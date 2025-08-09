package com.sliteptyltd.slite.utils.bindingadapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.feature.effectsservice.Effect.Companion.getGradientDrawable
import com.sliteptyltd.slite.utils.color.getColorWithSaturation
import com.sliteptyltd.slite.utils.color.transformTemperatureToRGB

@BindingAdapter("lightConfiguration", "lightStatus", requireAll = true)
fun ImageButton.setLightPowerButtonImage(lightConfiguration: LightConfiguration, lightStatus: LightStatus) {
    if (lightConfiguration.configurationMode == EFFECTS) {
        background = lightConfiguration.effect.getGradientDrawable(resources)
    } else {
        setBackgroundResource(R.drawable.bg_power_button)
    }
    backgroundTintList = when (lightStatus) {
        ON -> {
            setImageResource(R.drawable.ic_power)
            when (lightConfiguration.configurationMode) {
                WHITE -> {
                    ColorStateList.valueOf(transformTemperatureToRGB(lightConfiguration.temperature))
                }
                COLORS -> ColorStateList.valueOf(
                    getColorWithSaturation(
                        Color.parseColor(lightConfiguration.colorRgbHex),
                        lightConfiguration.saturation
                    )
                )
                EFFECTS -> null
            }
        }
        OFF -> {
            setImageResource(R.drawable.ic_power)
            ColorStateList.valueOf(resources.getColor(R.color.lights_list_light_power_button_off_tint, null))
        }
        DISCONNECTED -> {
            setImageResource(R.drawable.ic_info)
            ColorStateList.valueOf(resources.getColor(R.color.lights_list_light_power_button_disconnected_tint, null))
        }
    }
}

@BindingAdapter("isLightDropdownVisible")
fun View.setIsLightDropdownVisible(isLightDropdownVisible: Boolean) {
    isVisible = isLightDropdownVisible
}