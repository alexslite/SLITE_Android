package com.sliteptyltd.persistence.scene

import androidx.room.Embedded
import androidx.room.Relation
import com.sliteptyltd.persistence.Constants.ScenesDao.SCENE_ID_COLUMN

data class SceneWithLights(
    @Embedded val scene: SceneEntity,
    @Relation(
        parentColumn = SCENE_ID_COLUMN,
        entityColumn = SCENE_ID_COLUMN
    )
    val lights: List<SceneLightEntity>
)