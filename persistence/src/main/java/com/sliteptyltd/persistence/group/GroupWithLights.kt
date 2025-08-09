package com.sliteptyltd.persistence.group

import androidx.room.Embedded
import androidx.room.Relation
import com.sliteptyltd.persistence.Constants.GroupsDao.GROUP_ID_COLUMN

data class GroupWithLights(
    @Embedded
    val group: GroupEntity,
    @Relation(
        parentColumn = GROUP_ID_COLUMN,
        entityColumn = GROUP_ID_COLUMN
    )
    val lights: List<GroupedLightEntity>
)