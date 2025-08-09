package com.sliteptyltd.slite.feature.lights.adapter.viewholder

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem

abstract class LightsListItemViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(lightsListItem: LightsListItem)
}