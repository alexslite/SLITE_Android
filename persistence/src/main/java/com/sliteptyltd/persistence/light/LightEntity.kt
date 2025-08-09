package com.sliteptyltd.persistence.light

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHTS_TABLE
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_ADDRESS_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_ID_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_NAME_COLUMN
import com.sliteptyltd.persistence.Constants.LightsDao.LIGHT_STATUS_COLUMN

@Entity(tableName = LIGHTS_TABLE)
data class LightEntity(
    @PrimaryKey
    @ColumnInfo(name = LIGHT_ID_COLUMN)
    val id: Int,
    @ColumnInfo(name = LIGHT_NAME_COLUMN)
    val name: String,
    @ColumnInfo(name = LIGHT_STATUS_COLUMN)
    val status: String,
    @Embedded
    val lightConfiguration: LightEntityConfiguration,
    @ColumnInfo(name = LIGHT_ADDRESS_COLUMN)
    val address: String?,
    val batteryPercentage: Float? = null
)