package com.sliteptyltd.persistence.group

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sliteptyltd.persistence.Constants.GroupsDao.GROUPED_LIGHTS_TABLE

@Dao
interface GroupedLightsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groupsList: List<GroupedLightEntity>)

    @Query("DELETE FROM $GROUPED_LIGHTS_TABLE")
    suspend fun deleteAll()

    @Transaction
    suspend fun storeUpdatedGroupedLights(groupedLights: List<GroupedLightEntity>) {
        deleteAll()
        insertAll(groupedLights)
    }
}