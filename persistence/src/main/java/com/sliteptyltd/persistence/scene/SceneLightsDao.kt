package com.sliteptyltd.persistence.scene

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sliteptyltd.persistence.Constants.ScenesDao.SCENE_LIGHTS_TABLE

@Dao
interface SceneLightsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scenesList: List<SceneLightEntity>)

    @Query("DELETE FROM $SCENE_LIGHTS_TABLE")
    suspend fun deleteAll()

    @Transaction
    suspend fun storeUpdatedSceneLights(scenesList: List<SceneLightEntity>) {
        deleteAll()
        insertAll(scenesList)
    }
}