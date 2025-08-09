package com.sliteptyltd.slite.feature.scenes.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.SceneItemBinding
import com.sliteptyltd.slite.utils.extensions.views.layoutInflater

typealias OnScenePowerButtonClick = (sceneId: Int) -> Unit
typealias OnRenameSceneClick = (sceneId: Int, currentName: String) -> Unit
typealias OnDeleteSceneClick = (sceneId: Int, sceneName: String) -> Unit

class ScenesAdapter(
    private val onScenePowerButtonClick: OnScenePowerButtonClick,
    private val onRenameSceneClick: OnRenameSceneClick,
    private val onDeleteSceneClick: OnDeleteSceneClick
) : RecyclerSwipeAdapter<SceneViewHolder>() {

    private val swipeableItemsManager = SwipeItemRecyclerMangerImpl(this)
    private var scenes: List<SceneListItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SceneViewHolder =
        SceneViewHolder(
            SceneItemBinding.inflate(parent.layoutInflater, parent, false),
            onScenePowerButtonClick,
            onRenameSceneClick,
            onDeleteSceneClick
        )

    override fun onBindViewHolder(holder: SceneViewHolder, position: Int) {
        holder.bind((getItem(position)))
        swipeableItemsManager.bindView(holder.itemView, position)
    }

    fun updateScenesListItems(scenes: List<SceneListItem>) {
        closeSwipeLayouts()
        val diffCallback = ScenesListItemDiffUtilCallback(this.scenes, scenes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.scenes = scenes
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getItem(position: Int): SceneListItem = scenes[position]

    override fun getItemCount(): Int = scenes.size

    override fun getSwipeLayoutResourceId(position: Int): Int = R.id.sceneSwipeLayout

    private fun closeSwipeLayouts() {
        swipeableItemsManager.closeAllItems()
    }
}