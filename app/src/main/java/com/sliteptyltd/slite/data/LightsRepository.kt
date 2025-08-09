package com.sliteptyltd.slite.data

import com.sliteptyltd.persistence.group.GroupedLightsDao
import com.sliteptyltd.persistence.group.GroupsDao
import com.sliteptyltd.persistence.light.LightsDao
import com.sliteptyltd.persistence.scene.SceneLightsDao
import com.sliteptyltd.persistence.scene.ScenesDao
import com.sliteptyltd.slite.data.model.extensions.extractSceneLightsFromGroup
import com.sliteptyltd.slite.data.model.extensions.getIndividualLightsList
import com.sliteptyltd.slite.data.model.extensions.getLightsGroups
import com.sliteptyltd.slite.data.model.extensions.getListOfUngroupedLights
import com.sliteptyltd.slite.data.model.extensions.getStatusForGrouping
import com.sliteptyltd.slite.data.model.extensions.isLightsGroupItem
import com.sliteptyltd.slite.data.model.extensions.isSingleLightItem
import com.sliteptyltd.slite.data.model.extensions.mapToGroupedLightsEntities
import com.sliteptyltd.slite.data.model.extensions.mapToScenesLightsEntities
import com.sliteptyltd.slite.data.model.extensions.toGroupEntity
import com.sliteptyltd.slite.data.model.extensions.toLight
import com.sliteptyltd.slite.data.model.extensions.toLightEntity
import com.sliteptyltd.slite.data.model.extensions.toLightsGroup
import com.sliteptyltd.slite.data.model.extensions.toScene
import com.sliteptyltd.slite.data.model.extensions.toSceneEntity
import com.sliteptyltd.slite.data.model.extensions.toSceneLightDetailsDao
import com.sliteptyltd.slite.data.model.extensions.updateLightGroupPowerState
import com.sliteptyltd.slite.data.model.extensions.updateSingleLightPowerState
import com.sliteptyltd.slite.data.model.light.GroupLightDetails
import com.sliteptyltd.slite.data.model.light.Light
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.model.scene.Scene
import com.sliteptyltd.slite.data.model.scene.SceneLightDetails
import java.util.UUID

class LightsRepository(
    private val lightsDao: LightsDao,
    private val groupsDao: GroupsDao,
    private val groupedLightsDao: GroupedLightsDao,
    private val scenesDao: ScenesDao,
    private val sceneLightsDao: SceneLightsDao
) {

    private val lightsList = mutableListOf<Light>()
    private val scenesList = mutableListOf<Scene>()

    suspend fun storeLatestLightsList(clearEffectsMode: Boolean = false) {
        val individualLights = getIndividualLights()
        val groupedLights = getLightsGroups()

        lightsDao.storeUpdatedLights(individualLights.map { it.toLightEntity(clearEffectsMode) })

        groupsDao.storeUpdatedGroups(groupedLights.map { it.toGroupEntity(clearEffectsMode) })
        groupedLightsDao.storeUpdatedGroupedLights(groupedLights.mapToGroupedLightsEntities())
    }

    suspend fun fetchLights(): Boolean {
        lightsList.clear()

        lightsList.addAll(lightsDao.getLights().map { it.toLight() })
        lightsList.addAll(groupsDao.getGroupsWithLights().map { it.toLightsGroup() })

        return lightsList.isNotEmpty()
    }

    suspend fun storeLatestScenes() {
        scenesDao.storeUpdatedScenes(scenesList.map { it.toSceneEntity() })
        sceneLightsDao.storeUpdatedSceneLights(scenesList.mapToScenesLightsEntities())
    }

    suspend fun fetchScenes(): Boolean {
        scenesList.clear()
        scenesList.addAll(scenesDao.getScenesWithLights().map { it.toScene() })
        return scenesList.isNotEmpty()
    }

    fun getIndividualLights(): List<Light> = lightsList.getIndividualLightsList()

    fun getLightsGroups(): List<Light> = lightsList.getLightsGroups()

    fun addIndividualLight(light: Light): List<Light> {
        lightsList.add(light)

        return lightsList.getIndividualLightsList()
    }

    fun deleteIndividualLight(lightId: Int): List<Light> {
        lightsList.removeIf { it.id == lightId }
        scenesList.removeIf { it.containsLight(lightId) }

        return lightsList.getIndividualLightsList()
    }

    fun createGroup(groupName: String, groupedLightsIds: List<Int>): Int {
        val groupedLights = lightsList.filter { it.id in groupedLightsIds }
        lightsList.removeIf { it.id in groupedLightsIds }
        scenesList.removeIf { it.containsGroup(groupedLightsIds) }

        val groupStatus = groupedLights.getStatusForGrouping()
        val groupId = UUID.randomUUID().hashCode()
        lightsList.add(
            Light(
                groupId,
                LightConfiguration(
                    groupName,
                    lightsDetails = groupedLights.map {
                        GroupLightDetails(
                            it.id,
                            it.lightConfiguration.name,
                            it.getStatusForGrouping(groupStatus),
                            it.address,
                            WHITE
                        )
                    }
                ),
                ON,
                null
            )
        )
        return groupId
    }

    fun ungroupLights(groupId: Int): Boolean {
        val group = lightsList.firstOrNull { it.id == groupId && it.isLightsGroupItem() } ?: return false

        lightsList.removeIf { it.id == groupId }
        lightsList.addAll(group.getListOfUngroupedLights())
        val groupedLightsIds = group.lightConfiguration.lightsDetails.map { it.lightId }
        scenesList.removeIf { it.containsGroup(groupedLightsIds) }

        return true
    }

    fun updateLight(lightId: Int, configuration: LightConfiguration, status: LightStatus): Boolean {
        val light = lightsList.firstOrNull { it.id == lightId } ?: return false
        val result = lightsList.removeIf { it.id == lightId }
        if (result) {
            lightsList.add(light.copy(lightConfiguration = configuration, status = status))
        }
        return result
    }

    fun updateIndividualLight(light: Light): List<Light> {
        val result = lightsList.removeIf { it.id == light.id && it.isSingleLightItem() }
        if (result) lightsList.add(light)

        return lightsList.getIndividualLightsList()
    }

    fun updateLightsGroup(light: Light): List<Light> {
        val result = lightsList.removeIf { it.id == light.id && it.isLightsGroupItem() }
        if (result) lightsList.add(light)

        return lightsList.getLightsGroups()
    }

    fun updateLightsSectionPowerStatus(isGroupsSection: Boolean, isSectionStateOn: Boolean): List<Light> =
        if (isGroupsSection) {
            lightsList.replaceAll { light ->
                if (light.isLightsGroupItem()) {
                    light.updateLightGroupPowerState(isSectionStateOn)
                } else {
                    light
                }
            }

            lightsList.getLightsGroups()
        } else {
            lightsList.replaceAll { light ->
                if (light.isSingleLightItem()) {
                    light.updateSingleLightPowerState(isSectionStateOn)
                } else {
                    light
                }
            }

            lightsList.getIndividualLightsList()
        }

    fun getLightById(lightId: Int): Light? =
        lightsList.firstOrNull { it.id == lightId }

    fun getIndividualLightById(lightId: Int): Light? =
        lightsList.firstOrNull { it.id == lightId && it.isSingleLightItem() }

    fun getLightsGroupById(groupId: Int): Light? =
        lightsList.firstOrNull { it.id == groupId && it.isLightsGroupItem() }

    fun getScenes(): List<Scene> = scenesList

    fun getCanCreateScenes(): Boolean =
        lightsList.any { it.status == ON }

    fun createScene(sceneName: String): List<Scene> {
        val sceneLights = mutableListOf<SceneLightDetails>()

        lightsList.sortedBy { it.lightConfiguration.name }.map {
            if (it.status == ON) {
                if (it.isSingleLightItem()) {
                    sceneLights.add(it.toSceneLightDetailsDao())
                } else {
                    sceneLights.addAll(it.extractSceneLightsFromGroup())
                }
            }
        }

        scenesList.add(Scene(UUID.randomUUID().hashCode(), sceneName, sceneLights))
        return scenesList
    }

    fun renameScene(sceneId: Int, newSceneName: String): List<Scene> {
        val scene = scenesList.firstOrNull { it.id == sceneId } ?: return scenesList
        val updatedScene = scene.copy(name = newSceneName)

        scenesList.removeIf { it.id == sceneId }
        scenesList.add(updatedScene)

        return scenesList
    }

    fun deleteScene(sceneId: Int): List<Scene> {
        scenesList.removeIf { it.id == sceneId }
        return scenesList
    }

    fun updateSceneLightsDetails(sceneLightsDetails: List<SceneLightDetails>) {
        val updatedLights = mutableListOf<Light>()
        lightsList.forEach { light ->
            sceneLightsDetails.forEach sceneLightsIteration@{ sceneLightDetails ->
                if (light.isSingleLightItem()) {
                    if (light.id != sceneLightDetails.lightId || light.status == DISCONNECTED) return@sceneLightsIteration
                    updatedLights.add(
                        light.copy(
                            lightConfiguration = sceneLightDetails.configuration.copy(name = light.lightConfiguration.name),
                            status = ON
                        )
                    )
                } else {
                    val groupedLight = light.lightConfiguration.lightsDetails.firstOrNull { it.lightId == sceneLightDetails.lightId }
                    if (groupedLight == null || groupedLight.status == DISCONNECTED || updatedLights.any { it.id == light.id }) return@sceneLightsIteration

                    val lightsDetails = light.lightConfiguration.applySceneConfigurationToGroupedLight(sceneLightDetails.lightId)
                    updatedLights.add(
                        light.copy(
                            lightConfiguration = sceneLightDetails.configuration.copy(
                                name = light.lightConfiguration.name,
                                lightsDetails = lightsDetails
                            ),
                            status = ON
                        )
                    )
                }
            }
        }
        val updatedLightsIds = updatedLights.map { it.id }
        lightsList.removeIf { it.id in updatedLightsIds }
        lightsList.addAll(updatedLights)
    }

    fun getScenesContainingLight(lightId: Int): List<Scene> {
        val light = lightsList.firstOrNull { it.id == lightId } ?: return emptyList()
        val containingScenes = if (light.isSingleLightItem()) {
            scenesList.filter { it.containsLight(lightId) }
        } else {
            val groupedLightsIds = light.lightConfiguration.lightsDetails.map { it.lightId }
            scenesList.filter { it.containsGroup(groupedLightsIds) }
        }

        return containingScenes
    }

    companion object {
        fun Scene.containsLight(lightId: Int): Boolean = lightsConfigurations.any { sceneLight -> sceneLight.lightId == lightId }

        fun Scene.containsGroup(groupedLights: List<Int>) = lightsConfigurations.any { sceneLight -> sceneLight.lightId in groupedLights }

        fun LightConfiguration.applySceneConfigurationToGroupedLight(sceneLightId: Int): List<GroupLightDetails> =
            lightsDetails.map { groupedLight ->
                if (groupedLight.lightId == sceneLightId && groupedLight.status != DISCONNECTED) {
                    groupedLight.copy(status = ON)
                } else {
                    groupedLight.copy()
                }
            }
    }
}