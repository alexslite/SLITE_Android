package com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem

abstract class LightConfigurationViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(lightConfiguration: LightConfigurationItem)
}