package com.sliteptyltd.slite.feature.lights.adapter.viewholder

import com.sliteptyltd.slite.LightsSectionHeaderBinding
import com.sliteptyltd.slite.feature.lights.adapter.OnSectionHeaderDropdownButtonClick
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem
import com.sliteptyltd.slite.utils.Constants.Lights.DROPDOWN_COLLAPSED_ROTATION
import com.sliteptyltd.slite.utils.Constants.Lights.DROPDOWN_EXPANDED_ROTATION
import com.sliteptyltd.slite.utils.extensions.views.rotateDropdownButton

class LightsSectionHeaderViewHolder(
    private val binding: LightsSectionHeaderBinding,
    private val onSectionHeaderDropdownButtonClick: OnSectionHeaderDropdownButtonClick
) : LightsListItemViewHolder(binding) {

    private var sectionHeader: LightsListItem.SectionHeader? = null

    init {
        binding.sectionDropdownBtn.setOnClickListener {
            sectionHeader?.let { sectionHeader ->
                val newExpandedState = !sectionHeader.isExpanded
                binding.sectionDropdownBtn.rotateDropdownButton(getEndRotation(newExpandedState)) {
                    onSectionHeaderDropdownButtonClick(sectionHeader.type, newExpandedState)
                }
            }
        }
    }

    override fun bind(lightsListItem: LightsListItem) {
        val sectionHeader = (lightsListItem as? LightsListItem.SectionHeader)
            ?: throw IllegalArgumentException("Item of type ${lightsListItem::class.java} could not be cast to LightsListItem.SectionHeader")

        this.sectionHeader = sectionHeader
        binding.sectionHeader = sectionHeader
        binding.sectionDropdownBtn.rotation = getEndRotation(sectionHeader.isExpanded)
    }

    private fun getEndRotation(isExpanded: Boolean): Float =
        if (isExpanded) DROPDOWN_COLLAPSED_ROTATION else DROPDOWN_EXPANDED_ROTATION
}