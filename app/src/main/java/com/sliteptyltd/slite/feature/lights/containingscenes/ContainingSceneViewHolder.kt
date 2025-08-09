package com.sliteptyltd.slite.feature.lights.containingscenes

import androidx.recyclerview.widget.RecyclerView
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.ItemContainingSceneBinding

class ContainingSceneViewHolder(private val binding: ItemContainingSceneBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(containingScene: ContainingScene) {
        binding.root.text =
            binding.root.resources.getString(R.string.delete_light_dialog_containing_scene_name_format, containingScene.name)
    }
}