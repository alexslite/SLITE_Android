package com.sliteptyltd.slite.feature.scenes.adapter

import androidx.recyclerview.widget.DiffUtil

class ScenesListItemDiffUtilCallback(private val oldList: List<SceneListItem>, private val newList: List<SceneListItem>) :
    DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]
}