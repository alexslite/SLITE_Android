package com.sliteptyltd.slite.data.model.extensions

import com.sliteptyltd.persistence.group.GroupEntity
import com.sliteptyltd.persistence.group.GroupWithLights
import com.sliteptyltd.persistence.group.GroupedLightEntity
import com.sliteptyltd.persistence.light.LightEntity
import com.sliteptyltd.persistence.light.LightEntityConfiguration
import com.sliteptyltd.slite.data.model.scene.SceneLightDetails
import com.sliteptyltd.slite.data.model.light.GroupLightDetails
import com.sliteptyltd.slite.data.model.light.Light
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.model.light.LightStatus.valueOf
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.EffectsPlayerService.Companion.NO_GROUP_ID
import com.sliteptyltd.slite.feature.lights.adapter.data.LightUIConfiguration
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_BRIGHTNESS
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_HUE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_SATURATION
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_LIGHT_TEMPERATURE
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic

fun List<Light>.getIndividualLightsList(): List<Light> = filter { it.isSingleLightItem() }

fun List<Light>.getLightsGroups(): List<Light> = filter { it.isLightsGroupItem() }

fun Light.isSingleLightItem(): Boolean = lightConfiguration.isIndividualLight

fun Light.isLightsGroupItem(): Boolean = lightConfiguration.isLightsGroup

fun List<Light>.asOrderedLights(): List<Light> =
    sortedWith(
        compareBy(
            { it.status == DISCONNECTED },
            { it.lightConfiguration.name }
        )
    )

fun List<Light>.mapToLightItemsList(lightsUIConfigurations: Map<Int, LightUIConfiguration>): List<LightsListItem.LightItem> =
    map { it.toLightItem(lightsUIConfigurations[it.id] ?: LightUIConfiguration()) }

fun Light.toLightItem(lightUIConfiguration: LightUIConfiguration = LightUIConfiguration()): LightsListItem.LightItem =
    LightsListItem.LightItem(
        id,
        lightConfiguration,
        status,
        lightUIConfiguration,
        batteryPercentage
    )

fun Light.updateSingleLightPowerState(isPoweredOn: Boolean): Light =
    if (isSingleLightItem() && status != DISCONNECTED) {
        val newStatus = if (isPoweredOn) ON else OFF
        copy(status = newStatus)
    } else {
        this
    }

fun Light.updateLightGroupPowerState(isPoweredOn: Boolean): Light =
    if (isLightsGroupItem() && status != DISCONNECTED) {
        val newStatus = if (isPoweredOn) ON else OFF
        copy(
            status = newStatus,
            lightConfiguration = lightConfiguration.copy(
                lightsDetails = lightConfiguration.lightsDetails.map { it.updatePoweredOnStatus(newStatus) }
            )
        )
    } else {
        this
    }

fun Light.getStatusForGrouping(groupStatus: LightStatus): LightStatus =
    if (status == DISCONNECTED) status else groupStatus

fun List<Light>.getStatusForGrouping(): LightStatus =
    when {
        all { it.status == DISCONNECTED } -> DISCONNECTED
        any { it.status == ON } -> ON
        else -> OFF
    }

fun Light.toSceneLightDetailsDao(): SceneLightDetails =
    SceneLightDetails(id, lightConfiguration.copy(), address, NO_GROUP_ID)

fun Light.extractSceneLightsFromGroup(): List<SceneLightDetails> =
    lightConfiguration.lightsDetails.map {
        SceneLightDetails(
            it.lightId, lightConfiguration.copy(
                name = it.name,
                lightsDetails = listOf()
            ),
            it.address,
            id
        )
    }

fun Light.toLightEntity(clearEffectsMode: Boolean = false): LightEntity =
    LightEntity(
        id,
        lightConfiguration.name,
        status.toString(),
        lightConfiguration.toLightEntityConfiguration(clearEffectsMode),
        address
    )

fun LightConfiguration.toLightEntityConfiguration(clearEffectsMode: Boolean = false): LightEntityConfiguration {
    val storedEffect = if (clearEffectsMode) null else effect
    val storedConfigurationMode = if (clearEffectsMode && configurationMode == EFFECTS) COLORS else configurationMode

    return LightEntityConfiguration(
        hue,
        brightness,
        temperature,
        saturation,
        storedEffect?.toString(),
        storedConfigurationMode.toString()
    )
}

fun LightEntity.toLight(): Light =
    Light(
        id,
        lightConfiguration.toLightConfiguration(name),
        LightStatus.valueOf(status),
        address
    )

fun LightEntityConfiguration.toLightConfiguration(lightName: String): LightConfiguration =
    LightConfiguration(
        lightName,
        hue,
        brightness,
        temperature,
        saturation,
        Effect.fromString(effect),
        LightConfigurationMode.valueOf(configurationMode)
    )

fun LightEntityConfiguration.toGroupLightConfiguration(lightName: String, lightsDetails: List<GroupLightDetails>): LightConfiguration =
    LightConfiguration(
        lightName,
        hue,
        brightness,
        temperature,
        saturation,
        Effect.fromString(effect),
        LightConfigurationMode.valueOf(configurationMode),
        lightsDetails
    )

fun GroupWithLights.toLightsGroup(): Light =
    Light(
        group.id,
        group.lightConfiguration.toGroupLightConfiguration(
            group.name,
            lights.map {
                GroupLightDetails(
                    it.lightId,
                    it.lightName,
                    valueOf(it.status),
                    it.address,
                    LightConfigurationMode.valueOf(group.lightConfiguration.configurationMode)
                )
            }),
        valueOf(group.status),
        null
    )

fun Light.toGroupEntity(clearEffectsMode: Boolean = false): GroupEntity =
    GroupEntity(
        id,
        lightConfiguration.name,
        status.toString(),
        lightConfiguration.toLightEntityConfiguration(clearEffectsMode)
    )

fun GroupLightDetails.toGroupedLightEntity(groupId: Int): GroupedLightEntity =
    GroupedLightEntity(lightId, groupId, name, status.toString(), address)

fun List<Light>.mapToGroupedLightsEntities(): List<GroupedLightEntity> {
    val groupedLightsEntities = mutableListOf<GroupedLightEntity>()
    forEach { light ->
        groupedLightsEntities.addAll(
            light.lightConfiguration.lightsDetails.map { it.toGroupedLightEntity(light.id) }
        )
    }
    return groupedLightsEntities
}

fun Light.getListOfUngroupedLights(): List<Light> =
    lightConfiguration.lightsDetails.map { groupLightDetails ->
        Light(
            groupLightDetails.lightId,
            LightConfiguration(
                groupLightDetails.name,
                hue = lightConfiguration.hue,
                brightness = lightConfiguration.brightness,
                temperature = lightConfiguration.temperature,
                saturation = lightConfiguration.saturation,
                effect = lightConfiguration.effect,
                configurationMode = lightConfiguration.configurationMode
            ),
            groupLightDetails.status,
            groupLightDetails.address
        )
    }

fun List<Light>.getGroupContainingLightId(lightId: Int): Light? =
    firstOrNull { it.lightConfiguration.lightsDetails.any { light -> light.lightId == lightId } }

fun LightConfiguration.getGroupedLightsAddresses(): List<String> =
    lightsDetails.mapNotNull { it.address }

fun Light.extractSliteLightCharacteristic(blackout: Byte? = null): SliteLightCharacteristic =
    lightConfiguration.toSliteLightCharacteristic(blackout ?: if (status == ON) 0 else 1)

fun LightConfiguration.toSliteLightCharacteristic(blackout: Byte): SliteLightCharacteristic =
    SliteLightCharacteristic(
        temperature,
        hue,
        saturation.toFloat(),
        brightness.toFloat(),
        blackout
    )

fun LightConfiguration.isDefaultConfiguration(): Boolean =
    hue == DEFAULT_LIGHT_HUE &&
            brightness == DEFAULT_LIGHT_BRIGHTNESS &&
            temperature == DEFAULT_LIGHT_TEMPERATURE &&
            saturation == DEFAULT_LIGHT_SATURATION

val LightConfiguration.groupDisconnectedLightsCount: Int get() = lightsDetails.count { it.status == DISCONNECTED }

val LightConfiguration.groupOffLightsCount: Int get() = lightsDetails.count { it.status == OFF }