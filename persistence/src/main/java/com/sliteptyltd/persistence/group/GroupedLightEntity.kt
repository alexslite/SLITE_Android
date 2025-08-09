package com.sliteptyltd.persistence.group

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sliteptyltd.persistence.Constants
import com.sliteptyltd.persistence.Constants.GroupsDao.GROUP_ID_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_ADDRESS_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_ID_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_NAME_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_STATUS_COLUMN

@Entity(tableName = Constants.GroupsDao.GROUPED_LIGHTS_TABLE)
data class GroupedLightEntity(
    @PrimaryKey
    @ColumnInfo(name = LIGHT_ID_COLUMN)
    val lightId: Int,
    @ColumnInfo(name = GROUP_ID_COLUMN)
    val groupId: Int,
    @ColumnInfo(name = LIGHT_NAME_COLUMN)
    val lightName: String,
    @ColumnInfo(name = LIGHT_STATUS_COLUMN)
    val status: String,
    @ColumnInfo(name = LIGHT_ADDRESS_COLUMN)
    val address: String?
)
