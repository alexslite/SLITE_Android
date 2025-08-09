package com.sliteptyltd.persistence

object Constants {

    object LightsDao {
        const val LIGHTS_TABLE = "Lights"
        const val LIGHT_ID_COLUMN = "LightId"
        const val LIGHT_NAME_COLUMN = "LightName"
        const val LIGHT_STATUS_COLUMN = "LightStatus"
        const val LIGHT_ADDRESS_COLUMN = "LightAddress"
    }

    object GroupsDao {
        const val GROUPS_TABLE = "Groups"
        const val GROUP_ID_COLUMN = "GroupId"

        const val GROUPED_LIGHTS_TABLE = "GroupedLights"
    }

    object ScenesDao {
        const val SCENES_TABLE = "Scene"
        const val SCENE_ID_COLUMN = "SceneId"

        const val SCENE_LIGHTS_TABLE = "SceneLights"
    }

    const val SLITE_DATABASE_NAME = "slite_database.db"
}