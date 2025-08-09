package com.sliteptyltd.persistence.scene

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_ADDRESS_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_ID_COLUMN
import com.sliteptyltd.persistence.Constants.ScenesDao.SCENE_ID_COLUMN
import com.sliteptyltd.persistence.Constants.ScenesDao.SCENE_LIGHTS_TABLE
import com.sliteptyltd.persistence.light.LightEntityConfiguration

@Entity(tableName = SCENE_LIGHTS_TABLE)
data class SceneLightEntity(
    @PrimaryKey
    @ColumnInfo(name = "SceneLightId")
    val sceneLightId: String,
    @ColumnInfo(name = LIGHT_ID_COLUMN)
    val lightId: Int,
    @ColumnInfo(name = SCENE_ID_COLUMN)
    val sceneId: Int,
    @ColumnInfo(name = "LightName")
    val lightName: String,
    @Embedded
    val configuration: LightEntityConfiguration,
    @ColumnInfo(name = LIGHT_ADDRESS_COLUMN)
    val address: String?,
    @ColumnInfo(name = "SceneLightGroupId")
    val sceneLightGroupId: Int
)
