package com.sliteptyltd.slite.feature.lights.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType
import com.sliteptyltd.slite.feature.lights.adapter.viewholder.LightItemViewHolder
import com.sliteptyltd.slite.feature.lights.adapter.viewholder.LightsListItemViewHolder
import com.sliteptyltd.slite.feature.lights.adapter.viewholder.LightsSectionControlViewHolder
import com.sliteptyltd.slite.feature.lights.adapter.viewholder.LightsSectionHeaderViewHolder
import com.sliteptyltd.slite.utils.extensions.views.inflate

typealias OnSectionHeaderDropdownButtonClick = (sectionType: LightsSectionType, newDropdownState: Boolean) -> Unit
typealias OnSectionControlActionClick = (sectionType: LightsSectionType, isSectionStateOn: Boolean) -> Unit
typealias OnRenameLightClick = (lightId: Int, isLightsGroup: Boolean, currentName: String) -> Unit
typealias OnDeleteLightClick = (lightId: Int, lightName: String, isLightsGroup: Boolean) -> Unit
typealias OnLightPowerButtonClick = (lightId: Int, currentStatus: LightStatus) -> Unit
typealias OnLightConfigurationButtonClick = (lightId: Int) -> Unit
typealias CloseAllSwipeableLayoutsCallback = () -> Unit
typealias OnLightConfigurationUpdated = (lightId: Int, lightConfiguration: LightConfiguration) -> Unit
typealias OnTrackConfigurationChangedEvent = (lightId: Int) -> Unit

class LightsListAdapter(
    private val onSectionHeaderDropdownButtonClick: OnSectionHeaderDropdownButtonClick,
    private val onSectionControlActionClick: OnSectionControlActionClick,
    private val onRenameLightClick: OnRenameLightClick,
    private val onDeleteLightClick: OnDeleteLightClick,
    private val onLightPowerButtonClick: OnLightPowerButtonClick,
    private val onLightConfigurationButtonClick: OnLightConfigurationButtonClick,
    private val onLightConfigurationUpdated: OnLightConfigurationUpdated,
    private val onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
) : RecyclerSwipeAdapter<LightsListItemViewHolder>() {

    private val swipeableItemsManager = SwipeItemRecyclerMangerImpl(this)
    private var lightItems: List<LightsListItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightsListItemViewHolder =
        when (viewType) {
            LIGHT_ITEM_TYPE -> LightItemViewHolder(
                parent.inflate(LIGHT_ITEM_TYPE),
                onRenameLightClick,
                onDeleteLightClick,
                onLightPowerButtonClick,
                onLightConfigurationButtonClick,
                onLightConfigurationUpdated,
                onTrackConfigurationChangedEvent,
                ::closeSwipeLayouts
            )
            LIGHTS_SECTION_HEADER_TYPE -> LightsSectionHeaderViewHolder(
                parent.inflate(LIGHTS_SECTION_HEADER_TYPE),
                onSectionHeaderDropdownButtonClick
            )
            LIGHTS_SECTION_CONTROL_ITEM_TYPE -> LightsSectionControlViewHolder(
                parent.inflate(LIGHTS_SECTION_CONTROL_ITEM_TYPE),
                onSectionControlActionClick
            )
            else -> throw IllegalArgumentException("ViewType $viewType not handled")
        }

    override fun onBindViewHolder(holder: LightsListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (getItemViewType(position) == LIGHT_ITEM_TYPE) {
            swipeableItemsManager.bindView(holder.itemView, position)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is LightsListItem.SectionHeader -> LIGHTS_SECTION_HEADER_TYPE
            is LightsListItem.LightItem -> LIGHT_ITEM_TYPE
            is LightsListItem.LightsSectionControlItem -> LIGHTS_SECTION_CONTROL_ITEM_TYPE
        }

    fun updateLightsListItems(lightItems: List<LightsListItem>) {
        closeSwipeLayouts()
        val diffCallback = LightListItemsDiffUtilCallback(this.lightItems, lightItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.lightItems = lightItems
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getItem(position: Int): LightsListItem = lightItems[position]

    override fun getItemCount(): Int = lightItems.size

    override fun getSwipeLayoutResourceId(position: Int): Int = R.id.swipeLayout

    private fun closeSwipeLayouts() {
        swipeableItemsManager.closeAllItems()
    }

    companion object {
        private const val LIGHTS_SECTION_HEADER_TYPE = R.layout.item_lights_section_header
        private const val LIGHT_ITEM_TYPE = R.layout.item_light
        private const val LIGHTS_SECTION_CONTROL_ITEM_TYPE = R.layout.item_lights_section_control
    }
}