package com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.MotionEvent.ACTION_UP
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.updateLayoutParams
import com.google.android.material.slider.Slider
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.ItemColorConfigurationBinding
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnColorHexInputClick
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnConfigurationValueUpdated
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnOpenCameraColorPickerClick
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnTrackConfigurationChangedEvent
import com.sliteptyltd.slite.utils.Constants.ColorUtils.COLOR_HEX_PREFIX
import com.sliteptyltd.slite.utils.Constants.Lights.GRADIENT_TRACK_HEIGHT_DIFFERENCE
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_ITEM_PERCENT_TEXT_FORMAT
import com.sliteptyltd.slite.utils.OnSliderTouchListener
import com.sliteptyltd.slite.utils.color.blue
import com.sliteptyltd.slite.utils.color.colorSaturation
import com.sliteptyltd.slite.utils.color.colorWithMaxSaturation
import com.sliteptyltd.slite.utils.color.colorWithMinSaturation
import com.sliteptyltd.slite.utils.color.getColorWithSaturation
import com.sliteptyltd.slite.utils.color.green
import com.sliteptyltd.slite.utils.color.red
import com.sliteptyltd.slite.utils.color.toRgbHexString
import com.sliteptyltd.slite.utils.extensions.views.layoutInflater
import kotlin.math.roundToInt

@SuppressLint("ClickableViewAccessibility")
class ColorConfigurationViewHolder(
    private val binding: ItemColorConfigurationBinding,
    private val onConfigurationValueUpdated: OnConfigurationValueUpdated,
    private val onColorHexInputClick: OnColorHexInputClick,
    private val onOpenCameraColorPickerClick: OnOpenCameraColorPickerClick,
    private val onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
) : LightConfigurationViewHolder(binding) {

    private var colorConfiguration: LightConfigurationItem.ColorConfigurationItem? = null
    private var shouldNotify: Boolean = false

    init {
        binding.saturationSlider.addOnChangeListener { _, saturation, isUserInput ->
            binding.saturationValueTV.text = String.format(LIGHT_ITEM_PERCENT_TEXT_FORMAT, saturation.toInt())
            val saturationConfiguration = colorConfiguration?.copy(saturation = saturation.toInt())
            if (saturationConfiguration == null || !isUserInput) return@addOnChangeListener

            val color = Color.valueOf(getColorWithSaturation(saturationConfiguration.color, saturation.toInt()))
            binding.colorWheel.setRgb(color.red, color.green, color.blue)
            colorConfiguration = saturationConfiguration
        }

        binding.colorWheel.colorChangeListener = { colorInt ->
            val saturation = colorInt.colorSaturation
            val updatedColorConfiguration =
                colorConfiguration?.copy(saturation = saturation, color = colorInt)
                    ?: LightConfigurationItem.ColorConfigurationItem(saturation, colorInt)
            if (saturation != colorConfiguration?.saturation) {
                binding.saturationSlider.value = saturation.toFloat()
            }
            colorConfiguration = updatedColorConfiguration

            setSaturationGradientColors(colorInt)
            if (shouldNotify) {
                onConfigurationValueUpdated(updatedColorConfiguration)
            }
        }

        binding.colorWheel.thumbColorCircleScale = COLOR_WHEEL_THUMB_CIRCLE_SCALE
        binding.saturationGradient.updateLayoutParams {
            height =
                (binding.root.resources.getDimension(R.dimen.light_configuration_slider_track_height) + GRADIENT_TRACK_HEIGHT_DIFFERENCE).roundToInt()
        }

        binding.openHexInputBtn.setOnClickListener {
            colorConfiguration?.let {
                onColorHexInputClick(it.color.toRgbHexString().removePrefix(COLOR_HEX_PREFIX))
            }
        }

        binding.openCameraBtn.setOnClickListener {
            onOpenCameraColorPickerClick()
        }

        binding.saturationSlider.addOnSliderTouchListener(object : OnSliderTouchListener() {
            override fun onStopTrackingTouch(slider: Slider) {
                super.onStopTrackingTouch(slider)
                trackColorChanged()
            }
        })

        binding.colorWheel.setOnTouchListener { _, motionEvent ->
            if (motionEvent?.action == ACTION_UP) trackColorChanged()
            return@setOnTouchListener false
        }
    }

    override fun bind(lightConfiguration: LightConfigurationItem) {
        shouldNotify = false
        val saturationConfiguration = (lightConfiguration as? LightConfigurationItem.ColorConfigurationItem)
            ?: throw IllegalArgumentException("Item of type ${lightConfiguration::class.java} could not be cast to LightConfigurationItem.ColorConfigurationItem")
        this.colorConfiguration = saturationConfiguration

        binding.saturationSlider.setCustomThumbDrawable(R.drawable.ic_slider_thumb)
        setSaturationUIValue(saturationConfiguration.saturation)

        val color = Color.valueOf(getColorWithSaturation(saturationConfiguration.color, saturationConfiguration.saturation))
        binding.colorWheel.setRgb(color.red, color.green, color.blue)
        setSaturationGradientColors(color.toArgb())

        shouldNotify = true
    }

    private fun setSaturationUIValue(saturation: Int) {
        binding.saturationValueTV.text = String.format(LIGHT_ITEM_PERCENT_TEXT_FORMAT, saturation)
        binding.saturationSlider.value = saturation.toFloat()
    }

    private fun setSaturationGradientColors(@ColorInt colorInt: Int) {
        binding.saturationGradient.startColor = colorInt.colorWithMinSaturation
        binding.saturationGradient.endColor = colorInt.colorWithMaxSaturation
    }

    private fun trackColorChanged() {
        val colorConfiguration =
            colorConfiguration?.copy(saturation = binding.saturationSlider.value.toInt(), color = binding.colorWheel.rgb) ?: return
        onTrackConfigurationChangedEvent(colorConfiguration)
    }

    companion object {
        private const val COLOR_WHEEL_THUMB_CIRCLE_SCALE = 0.9f

        fun newInstance(
            parent: ViewGroup,
            onConfigurationValueUpdated: OnConfigurationValueUpdated,
            onColorHexInputClick: OnColorHexInputClick,
            onOpenCameraColorPickerClick: OnOpenCameraColorPickerClick,
            onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
        ): ColorConfigurationViewHolder =
            ColorConfigurationViewHolder(
                ItemColorConfigurationBinding.inflate(parent.layoutInflater, parent, false),
                onConfigurationValueUpdated,
                onColorHexInputClick,
                onOpenCameraColorPickerClick,
                onTrackConfigurationChangedEvent
            )
    }
}