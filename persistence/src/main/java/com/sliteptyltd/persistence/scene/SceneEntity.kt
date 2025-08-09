package com.sliteptyltd.persistence.scene

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sliteptyltd.persistence.Constants.ScenesDao.SCENES_TABLE
import com.sliteptyltd.persistence.Constants.ScenesDao.SCENE_ID_COLUMN

@Entity(tableName = SCENES_TABLE)
data class SceneEntity(
    @PrimaryKey
    @ColumnInfo(name = SCENE_ID_COLUMN)
    val id: Int,
    @ColumnInfo(name = "SceneName")
    val name: String,
    @ColumnInfo(name = "CreatedAt")
    val createdAt: Long
)