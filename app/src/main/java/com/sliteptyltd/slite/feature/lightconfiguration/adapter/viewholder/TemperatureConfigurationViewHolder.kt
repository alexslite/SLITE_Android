package com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder

import android.view.ViewGroup
import com.google.android.material.slider.Slider
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.ItemTemperatureConfigurationBinding
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnConfigurationValueUpdated
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnTrackConfigurationChangedEvent
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_TEMPERATURE_TEXT_FORMAT
import com.sliteptyltd.slite.utils.OnSliderTouchListener
import com.sliteptyltd.slite.utils.extensions.views.layoutInflater

class TemperatureConfigurationViewHolder(
    private val binding: ItemTemperatureConfigurationBinding,
    private val onConfigurationValueUpdated: OnConfigurationValueUpdated,
    private val onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
) : LightConfigurationViewHolder(binding) {

    private var temperatureConfiguration: LightConfigurationItem.TemperatureConfigurationItem? = null
    private var shouldNotify = false

    init {
        binding.daylightTemperatureOptionRB.setOnClickListener {
            setTemperatureUIValue(TEMPERATURE_DAYLIGHT_VALUE)
            trackTemperatureChanged()
        }
        binding.coolWhiteTemperatureOptionRB.setOnClickListener {
            setTemperatureUIValue(TEMPERATURE_COOL_WHITE_VALUE)
            trackTemperatureChanged()
        }
        binding.tungstenTemperatureOptionRB.setOnClickListener {
            setTemperatureUIValue(TEMPERATURE_TUNGSTEN_VALUE)
            trackTemperatureChanged()
        }

        binding.temperatureSlider.addOnChangeListener { _, temperature, _ ->
            binding.temperatureValueTV.text = String.format(LIGHT_TEMPERATURE_TEXT_FORMAT, temperature.toInt())

            if (!shouldNotify) return@addOnChangeListener
            val updatedTemperatureConfiguration =
                temperatureConfiguration?.copy(temperature = temperature.toInt()) ?: return@addOnChangeListener
            temperatureConfiguration = updatedTemperatureConfiguration
            onConfigurationValueUpdated(updatedTemperatureConfiguration)
        }

        binding.temperatureSlider.addOnSliderTouchListener(object : OnSliderTouchListener() {
            override fun onStopTrackingTouch(slider: Slider) {
                super.onStopTrackingTouch(slider)
                trackTemperatureChanged()
            }
        })
    }

    override fun bind(lightConfiguration: LightConfigurationItem) {
        shouldNotify = false
        val temperatureConfiguration = (lightConfiguration as? LightConfigurationItem.TemperatureConfigurationItem)
            ?: throw IllegalArgumentException("Item of type ${lightConfiguration::class.java} could not be cast to LightConfigurationItem.TemperatureConfigurationItem")
        this.temperatureConfiguration = temperatureConfiguration

        binding.temperatureSlider.setCustomThumbDrawable(R.drawable.ic_slider_thumb)
        setTemperatureUIValue(temperatureConfiguration.temperature)
        shouldNotify = true
    }

    private fun setTemperatureUIValue(temperature: Int) {
        binding.temperatureValueTV.text = String.format(LIGHT_TEMPERATURE_TEXT_FORMAT, temperature)
        binding.temperatureSlider.value = temperature.toFloat()
    }

    private fun trackTemperatureChanged() {
        val temperatureConfiguration =
            temperatureConfiguration?.copy(temperature = binding.temperatureSlider.value.toInt()) ?: return
        onTrackConfigurationChangedEvent(temperatureConfiguration)
    }

    companion object {
        private const val TEMPERATURE_TUNGSTEN_VALUE = 3200
        private const val TEMPERATURE_DAYLIGHT_VALUE = 5600
        private const val TEMPERATURE_COOL_WHITE_VALUE = 7500

        fun newInstance(
            parent: ViewGroup,
            onConfigurationValueUpdated: OnConfigurationValueUpdated,
            onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
        ): TemperatureConfigurationViewHolder =
            TemperatureConfigurationViewHolder(
                ItemTemperatureConfigurationBinding.inflate(parent.layoutInflater, parent, false),
                onConfigurationValueUpdated,
                onTrackConfigurationChangedEvent
            )
    }
}