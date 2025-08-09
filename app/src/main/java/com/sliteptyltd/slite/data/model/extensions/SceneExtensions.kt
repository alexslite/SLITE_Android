package com.sliteptyltd.slite.data.model.extensions

import android.graphics.Color
import com.sliteptyltd.persistence.scene.SceneEntity
import com.sliteptyltd.persistence.scene.SceneLightEntity
import com.sliteptyltd.persistence.scene.SceneWithLights
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.scene.Scene
import com.sliteptyltd.slite.data.model.scene.SceneLightDetails
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.lights.containingscenes.ContainingScene
import com.sliteptyltd.slite.feature.scenes.adapter.SceneListItem
import com.sliteptyltd.slite.utils.Constants.Effects.defaultEffectGradientColors
import com.sliteptyltd.slite.utils.color.getColorWithSaturation
import com.sliteptyltd.slite.utils.color.transformTemperatureToRGB
import java.util.UUID
import org.joda.time.DateTime

fun List<Scene>.mapToSceneListItems(): List<SceneListItem> = sortedByDescending { it.createdAt }.map { it.toSceneListItem() }

fun Scene.toSceneListItem(): SceneListItem =
    SceneListItem(
        id,
        name,
        lightsConfigurations.getLightsColors().distinct().sortedDescending()
    )

fun List<SceneLightDetails>.getLightsColors(): List<Int> {
    val gradientColors = mutableListOf<Int>()
    forEach { lightConfiguration ->
        when (lightConfiguration.configuration.configurationMode) {
            WHITE -> gradientColors.add(transformTemperatureToRGB(lightConfiguration.configuration.temperature))
            COLORS -> gradientColors.add(
                getColorWithSaturation(
                    Color.parseColor(lightConfiguration.configuration.colorRgbHex),
                    lightConfiguration.configuration.saturation
                )
            )
            EFFECTS -> gradientColors.addAll(
                lightConfiguration.configuration.effect?.gradientColors?.toList() ?: defaultEffectGradientColors.toList()
            )
        }
    }
    return gradientColors
}

fun Scene.toSceneEntity(): SceneEntity =
    SceneEntity(
        id,
        name,
        createdAt.millis
    )

fun List<Scene>.mapToScenesLightsEntities(): List<SceneLightEntity> {
    val sceneLights = mutableListOf<SceneLightEntity>()
    forEach { scene ->
        sceneLights.addAll(scene.lightsConfigurations.map { light -> light.toSceneLightEntity(scene.id) })
    }
    return sceneLights
}

fun SceneLightDetails.toSceneLightEntity(sceneId: Int): SceneLightEntity =
    SceneLightEntity(
        UUID.randomUUID().toString(),
        lightId,
        sceneId,
        configuration.name,
        configuration.toLightEntityConfiguration(),
        address,
        groupId
    )

fun SceneWithLights.toScene(): Scene =
    Scene(
        scene.id,
        scene.name,
        lights.map { sceneLight -> sceneLight.toSceneLightDetails() },
        DateTime().withMillis(scene.createdAt)
    )

fun SceneLightEntity.toSceneLightDetails(): SceneLightDetails =
    SceneLightDetails(
        lightId,
        LightConfiguration(
            lightName,
            configuration.hue,
            configuration.brightness,
            configuration.temperature,
            configuration.saturation,
            Effect.fromString(configuration.effect),
            LightConfigurationMode.valueOf(configuration.configurationMode),
        ),
        address,
        sceneLightGroupId
    )

fun Scene.toContainingScene(): ContainingScene = ContainingScene(id, name)

fun List<Scene>.mapToContainingScenesList(): List<ContainingScene> = map { it.toContainingScene() }