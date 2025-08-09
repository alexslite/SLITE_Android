package com.sliteptyltd.persistence.light

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHTS_TABLE

@Dao
interface LightsDao {

    @Query("SELECT * FROM $LIGHTS_TABLE")
    suspend fun getLights(): List<LightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lightsList: List<LightEntity>)

    @Query("DELETE FROM $LIGHTS_TABLE")
    suspend fun deleteAll()

    @Transaction
    suspend fun storeUpdatedLights(lightsList: List<LightEntity>) {
        deleteAll()
        insertAll(lightsList)
    }
}