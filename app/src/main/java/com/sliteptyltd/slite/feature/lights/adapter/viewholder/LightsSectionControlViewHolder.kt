package com.sliteptyltd.slite.feature.lights.adapter.viewholder

import com.sliteptyltd.slite.LightsSectionControlItemBinding
import com.sliteptyltd.slite.feature.lights.adapter.OnSectionControlActionClick
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType

class LightsSectionControlViewHolder(
    binding: LightsSectionControlItemBinding,
    private val onSectionControlActionClick: OnSectionControlActionClick
) : LightsListItemViewHolder(binding) {

    private var sectionType: LightsSectionType? = null

    init {
        binding.turnOnBtn.setOnClickListener {
            sectionType?.let { sectionType ->
                onSectionControlActionClick(sectionType, true)
            }
        }

        binding.turnOffBtn.setOnClickListener {
            sectionType?.let { sectionType ->
                onSectionControlActionClick(sectionType, false)
            }
        }
    }

    override fun bind(lightsListItem: LightsListItem) {
        val sectionControlItem = (lightsListItem as? LightsListItem.LightsSectionControlItem)
            ?: throw IllegalArgumentException("Item of type ${lightsListItem::class.java} could not be cast to LightsListItem.LightsSectionControlItem")

        sectionType = sectionControlItem.type
    }
}