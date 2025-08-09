package com.sliteptyltd.slite.feature.lightconfiguration.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder.BrightnessConfigurationViewHolder
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder.LightConfigurationViewHolder
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder.ColorConfigurationViewHolder
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder.EffectsConfigurationViewHolder
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder.TemperatureConfigurationViewHolder
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem.BrightnessConfigurationItem
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem.TemperatureConfigurationItem
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem.ColorConfigurationItem
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem.EffectsConfigurationItem

typealias OnConfigurationValueUpdated = (lightConfiguration: LightConfigurationItem) -> Unit
typealias OnColorHexInputClick = (currentColorHex: String) -> Unit
typealias OnOpenCameraColorPickerClick = () -> Unit
typealias OnTrackConfigurationChangedEvent = (LightConfigurationItem) -> Unit

class LightConfigurationAdapter(
    private val onConfigurationValueUpdated: OnConfigurationValueUpdated,
    private val onColorHexInputClick: OnColorHexInputClick,
    private val onOpenCameraColorPickerClick: OnOpenCameraColorPickerClick,
    private val onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
) : ListAdapter<LightConfigurationItem, LightConfigurationViewHolder>(lightConfigurationItemsDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightConfigurationViewHolder =
        when (viewType) {
            LIGHT_BRIGHTNESS_TYPE -> BrightnessConfigurationViewHolder.newInstance(
                parent,
                onConfigurationValueUpdated,
                onTrackConfigurationChangedEvent
            )
            LIGHT_TEMPERATURE_TYPE -> TemperatureConfigurationViewHolder.newInstance(
                parent, onConfigurationValueUpdated,
                onTrackConfigurationChangedEvent
            )
            LIGHT_COLOR_TYPE -> ColorConfigurationViewHolder.newInstance(
                parent,
                onConfigurationValueUpdated,
                onColorHexInputClick,
                onOpenCameraColorPickerClick,
                onTrackConfigurationChangedEvent
            )
            LIGHT_EFFECTS_TYPE -> EffectsConfigurationViewHolder.newInstance(
                parent, onConfigurationValueUpdated,
                onTrackConfigurationChangedEvent
            )
            else -> throw IllegalArgumentException("ViewType $viewType not handled")
        }

    override fun onBindViewHolder(holder: LightConfigurationViewHolder, position: Int) = holder.bind(getItem(position))

    override fun onViewDetachedFromWindow(holder: LightConfigurationViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder as? EffectsConfigurationViewHolder)?.clearEffectSelection()
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is BrightnessConfigurationItem -> LIGHT_BRIGHTNESS_TYPE
            is TemperatureConfigurationItem -> LIGHT_TEMPERATURE_TYPE
            is ColorConfigurationItem -> LIGHT_COLOR_TYPE
            is EffectsConfigurationItem -> LIGHT_EFFECTS_TYPE
        }

    companion object {
        const val LIGHT_BRIGHTNESS_TYPE = R.layout.item_brightness_configuration
        const val LIGHT_TEMPERATURE_TYPE = R.layout.item_temperature_configuration
        const val LIGHT_COLOR_TYPE = R.layout.item_color_configuration
        const val LIGHT_EFFECTS_TYPE = R.layout.item_effects

        private val lightConfigurationItemsDiffUtil by lazy { initLightConfigurationItemsDiffUtil() }

        private fun initLightConfigurationItemsDiffUtil(): DiffUtil.ItemCallback<LightConfigurationItem> =
            object : DiffUtil.ItemCallback<LightConfigurationItem>() {
                override fun areItemsTheSame(oldItem: LightConfigurationItem, newItem: LightConfigurationItem): Boolean =
                    oldItem.itemId == newItem.itemId

                override fun areContentsTheSame(oldItem: LightConfigurationItem, newItem: LightConfigurationItem): Boolean =
                    oldItem == newItem
            }
    }
}