package com.sliteptyltd.persistence.group

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sliteptyltd.persistence.Constants.GroupsDao.GROUPS_TABLE
import com.sliteptyltd.persistence.Constants.GroupsDao.GROUP_ID_COLUMN
import com.sliteptyltd.persistence.light.LightEntityConfiguration

@Entity(tableName = GROUPS_TABLE)
data class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = GROUP_ID_COLUMN)
    val id: Int,
    @ColumnInfo(name = "GroupName")
    val name: String,
    @ColumnInfo(name = "GroupStatus")
    val status: String,
    @Embedded
    val lightConfiguration: LightEntityConfiguration,
)