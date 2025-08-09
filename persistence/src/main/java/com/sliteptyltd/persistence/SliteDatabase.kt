package com.sliteptyltd.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sliteptyltd.persistence.group.GroupEntity
import com.sliteptyltd.persistence.group.GroupedLightEntity
import com.sliteptyltd.persistence.group.GroupedLightsDao
import com.sliteptyltd.persistence.group.GroupsDao
import com.sliteptyltd.persistence.light.LightEntity
import com.sliteptyltd.persistence.light.LightsDao
import com.sliteptyltd.persistence.scene.SceneEntity
import com.sliteptyltd.persistence.scene.SceneLightEntity
import com.sliteptyltd.persistence.scene.SceneLightsDao
import com.sliteptyltd.persistence.scene.ScenesDao

@Database(
    entities = [
        LightEntity::class,
        GroupEntity::class,
        GroupedLightEntity::class,
        SceneEntity::class,
        SceneLightEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SliteDatabase : RoomDatabase() {

    abstract val lightsDao: LightsDao

    abstract val groupsDao: GroupsDao

    abstract val groupedLightsDao: GroupedLightsDao

    abstract val scenesDao: ScenesDao

    abstract val sceneLightsDao: SceneLightsDao
}