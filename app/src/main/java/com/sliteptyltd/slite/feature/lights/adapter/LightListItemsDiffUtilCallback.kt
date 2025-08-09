package com.sliteptyltd.slite.feature.lights.adapter

import androidx.recyclerview.widget.DiffUtil
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem

class LightListItemsDiffUtilCallback(private val oldList: List<LightsListItem>, private val newList: List<LightsListItem>) :
    DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].itemId == newList[newItemPosition].itemId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]
}