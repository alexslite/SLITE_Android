package com.sliteptyltd.slite.feature.scenes.adapter

import androidx.recyclerview.widget.RecyclerView
import com.sliteptyltd.slite.SceneItemBinding
import com.sliteptyltd.slite.utils.extensions.views.animateInvisible

class SceneViewHolder(
    private val binding: SceneItemBinding,
    private val onScenePowerButtonClick: OnScenePowerButtonClick,
    private val onRenameSceneClick: OnRenameSceneClick,
    private val onDeleteSceneClick: OnDeleteSceneClick
) : RecyclerView.ViewHolder(binding.root) {

    private var sceneListItem: SceneListItem? = null

    init {
        binding.scenePowerIB.setOnClickListener {
            sceneListItem?.id?.let { sceneId ->
                onScenePowerButtonClick(sceneId)
                binding.applySceneFeedbackIndicatorV.animateInvisible(
                    false,
                    FEEDBACK_INDICATOR_VISIBILITY_ANIMATION_DURATION,
                    onAnimationEnd = {
                        binding.applySceneFeedbackIndicatorV.postDelayed({
                            binding.applySceneFeedbackIndicatorV.animateInvisible(true, FEEDBACK_INDICATOR_VISIBILITY_ANIMATION_DURATION)
                        }, FEEDBACK_INDICATOR_DISPLAY_DURATION)
                    }
                )
            }
        }

        binding.optionsIB.setOnClickListener {
            binding.sceneSwipeLayout.open()
        }

        binding.editSceneNameBtn.root.setOnClickListener {
            binding.sceneSwipeLayout.close()
            sceneListItem?.let { scene -> onRenameSceneClick(scene.id, scene.name) }
        }

        binding.deleteSceneBtn.root.setOnClickListener {
            binding.sceneSwipeLayout.close()
            sceneListItem?.let { scene -> onDeleteSceneClick(scene.id, scene.name) }
        }
    }

    fun bind(sceneListItem: SceneListItem) {
        this.sceneListItem = sceneListItem
        binding.sceneListItem = sceneListItem
    }

    companion object {
        private const val FEEDBACK_INDICATOR_VISIBILITY_ANIMATION_DURATION = 250L
        private const val FEEDBACK_INDICATOR_DISPLAY_DURATION = 500L
    }
}