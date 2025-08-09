package com.sliteptyltd.persistence.scene

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sliteptyltd.persistence.Constants.ScenesDao.SCENES_TABLE

@Dao
interface ScenesDao {

    @Transaction
    @Query("SELECT * FROM $SCENES_TABLE")
    suspend fun getScenesWithLights(): List<SceneWithLights>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scenesList: List<SceneEntity>)

    @Query("DELETE FROM $SCENES_TABLE")
    suspend fun deleteAll()

    @Transaction
    suspend fun storeUpdatedScenes(scenesList: List<SceneEntity>) {
        deleteAll()
        insertAll(scenesList)
    }
}