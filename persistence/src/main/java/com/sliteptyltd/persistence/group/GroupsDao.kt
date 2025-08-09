package com.sliteptyltd.persistence.group

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sliteptyltd.persistence.Constants.GroupsDao.GROUPS_TABLE

@Dao
interface GroupsDao {

    @Transaction
    @Query("SELECT * FROM $GROUPS_TABLE")
    suspend fun getGroupsWithLights(): List<GroupWithLights>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groupsList: List<GroupEntity>)

    @Query("DELETE FROM $GROUPS_TABLE")
    suspend fun deleteAll()

    @Transaction
    suspend fun storeUpdatedGroups(groupsList: List<GroupEntity>) {
        deleteAll()
        insertAll(groupsList)
    }
}