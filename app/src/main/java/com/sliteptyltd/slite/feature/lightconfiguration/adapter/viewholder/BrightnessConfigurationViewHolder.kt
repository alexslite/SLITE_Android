package com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder

import android.view.ViewGroup
import com.google.android.material.slider.Slider
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.ItemBrightnessConfigurationBinding
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnConfigurationValueUpdated
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnTrackConfigurationChangedEvent
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_ITEM_PERCENT_TEXT_FORMAT
import com.sliteptyltd.slite.utils.OnSliderTouchListener
import com.sliteptyltd.slite.utils.extensions.views.layoutInflater

class BrightnessConfigurationViewHolder(
    private val binding: ItemBrightnessConfigurationBinding,
    private val onConfigurationValueUpdated: OnConfigurationValueUpdated,
    private val onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
) : LightConfigurationViewHolder(binding) {

    private var brightnessConfiguration: LightConfigurationItem.BrightnessConfigurationItem? = null
    private var shouldNotify = false

    init {
        binding.brightnessPresetsRG.setOnCheckedChangeListener { _, itemId ->
            when (itemId) {
                R.id.secondBrightnessOptionRB -> setBrightnessUIValue(SECOND_BRIGHTNESS_OPTION_VALUE)
                R.id.thirdBrightnessOptionRB -> setBrightnessUIValue(THIRD_BRIGHTNESS_OPTION_VALUE)
                R.id.fourthBrightnessOptionRB -> setBrightnessUIValue(FOURTH_BRIGHTNESS_OPTION_VALUE)
                R.id.fifthBrightnessOptionRB -> setBrightnessUIValue(FIFTH_BRIGHTNESS_OPTION_VALUE)
                else -> setBrightnessUIValue(FIRST_BRIGHTNESS_OPTION_VALUE)
            }
            val updatedBrightnessConfiguration = brightnessConfiguration ?: return@setOnCheckedChangeListener
            onTrackConfigurationChangedEvent(updatedBrightnessConfiguration)
        }

        binding.brightnessSlider.addOnChangeListener { _, brightness, _ ->
            binding.brightnessValueTV.text = String.format(LIGHT_ITEM_PERCENT_TEXT_FORMAT, brightness.toInt())
            if (!shouldNotify) return@addOnChangeListener

            val updatedBrightnessConfiguration =
                brightnessConfiguration?.copy(brightness = brightness.toInt()) ?: return@addOnChangeListener
            brightnessConfiguration = updatedBrightnessConfiguration
            onConfigurationValueUpdated(updatedBrightnessConfiguration)
        }

        binding.brightnessSlider.addOnSliderTouchListener(object : OnSliderTouchListener() {
            override fun onStopTrackingTouch(slider: Slider) {
                super.onStopTrackingTouch(slider)
                val updatedBrightnessConfiguration =
                    brightnessConfiguration?.copy(brightness = binding.brightnessSlider.value.toInt()) ?: return
                onTrackConfigurationChangedEvent(updatedBrightnessConfiguration)
            }
        })

        initBrightnessOptions()
    }

    override fun bind(lightConfiguration: LightConfigurationItem) {
        shouldNotify = false
        val brightnessConfiguration = (lightConfiguration as? LightConfigurationItem.BrightnessConfigurationItem)
            ?: throw IllegalArgumentException("Item of type ${lightConfiguration::class.java} could not be cast to LightConfigurationItem.BrightnessConfigurationItem")
        this.brightnessConfiguration = brightnessConfiguration

        binding.brightnessSlider.setCustomThumbDrawable(R.drawable.ic_slider_thumb)
        setBrightnessUIValue(brightnessConfiguration.brightness)
        shouldNotify = true
    }

    private fun setBrightnessUIValue(brightness: Int) {
        binding.brightnessValueTV.text = String.format(LIGHT_ITEM_PERCENT_TEXT_FORMAT, brightness)
        binding.brightnessSlider.value = brightness.toFloat()
    }

    private fun initBrightnessOptions() {
        binding.firstBrightnessOptionRB.text = BrightnessPreset.FIRST.formattedName
        binding.secondBrightnessOptionRB.text = BrightnessPreset.SECOND.formattedName
        binding.thirdBrightnessOptionRB.text = BrightnessPreset.THIRD.formattedName
        binding.fourthBrightnessOptionRB.text = BrightnessPreset.FOURTH.formattedName
        binding.fifthBrightnessOptionRB.text = BrightnessPreset.FIFTH.formattedName
    }

    enum class BrightnessPreset(val value: Int) {
        FIRST(FIRST_BRIGHTNESS_OPTION_VALUE),
        SECOND(SECOND_BRIGHTNESS_OPTION_VALUE),
        THIRD(THIRD_BRIGHTNESS_OPTION_VALUE),
        FOURTH(FOURTH_BRIGHTNESS_OPTION_VALUE),
        FIFTH(FIFTH_BRIGHTNESS_OPTION_VALUE);

        val formattedName: String
            get() = String.format(LIGHT_ITEM_PERCENT_TEXT_FORMAT, value)
    }

    companion object {
        private const val FIRST_BRIGHTNESS_OPTION_VALUE = 0
        private const val SECOND_BRIGHTNESS_OPTION_VALUE = 25
        private const val THIRD_BRIGHTNESS_OPTION_VALUE = 50
        private const val FOURTH_BRIGHTNESS_OPTION_VALUE = 75
        private const val FIFTH_BRIGHTNESS_OPTION_VALUE = 100

        fun newInstance(
            parent: ViewGroup,
            onConfigurationValueUpdated: OnConfigurationValueUpdated,
            onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
        ): BrightnessConfigurationViewHolder =
            BrightnessConfigurationViewHolder(
                ItemBrightnessConfigurationBinding.inflate(parent.layoutInflater, parent, false),
                onConfigurationValueUpdated,
                onTrackConfigurationChangedEvent
            )
    }
}