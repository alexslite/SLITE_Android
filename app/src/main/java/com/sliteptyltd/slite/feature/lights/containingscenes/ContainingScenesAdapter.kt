package com.sliteptyltd.slite.feature.lights.containingscenes

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sliteptyltd.slite.databinding.ItemContainingSceneBinding
import com.sliteptyltd.slite.utils.extensions.views.layoutInflater

class ContainingScenesAdapter : ListAdapter<ContainingScene, ContainingSceneViewHolder>(containingScenesDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainingSceneViewHolder =
        ContainingSceneViewHolder(ItemContainingSceneBinding.inflate(parent.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ContainingSceneViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val containingScenesDiffUtil by lazy {
            object : DiffUtil.ItemCallback<ContainingScene>() {
                override fun areItemsTheSame(oldItem: ContainingScene, newItem: ContainingScene): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: ContainingScene, newItem: ContainingScene): Boolean = oldItem == newItem
            }
        }
    }
}