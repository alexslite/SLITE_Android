package com.sliteptyltd.slite.feature.scenes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sliteptyltd.slite.data.model.light.Light
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.scene.Scene
import com.sliteptyltd.slite.data.usecase.GetIndividualLightsUseCase
import com.sliteptyltd.slite.data.usecase.GetLightsGroupsUseCase
import com.sliteptyltd.slite.data.usecase.caching.StoreLatestScenesUseCase
import com.sliteptyltd.slite.data.usecase.scenes.*
import com.sliteptyltd.slite.feature.effectsservice.EffectsPlayerService.Companion.NO_GROUP_ID
import kotlinx.coroutines.launch

class ScenesViewModel(
    private val storeLatestScenes: StoreLatestScenesUseCase,
    private val getScenes: GetScenesUseCase,
    private val createScene: CreateSceneUseCase,
    private val getCanCreateScene: GetCanCreateSceneUseCase,
    private val renameScene: RenameSceneUseCase,
    private val deleteScene: DeleteSceneUseCase,
    private val updateSceneLightsDetails: UpdateSceneLightsDetailsUseCase,
    private val getIndividualLights: GetIndividualLightsUseCase,
    private val getLightsGroups: GetLightsGroupsUseCase
) : ViewModel() {

    private val individualLights = MutableLiveData<List<Light>>(listOf())
    private val lightsGroups = MutableLiveData<List<Light>>(listOf())
    private val _scenes = MutableLiveData<List<Scene>>()
    val scenes: LiveData<List<Scene>>
        get() = _scenes

    val canCreateScene: Boolean get() = getCanCreateScene()

    init {
        getScenesList()
        getLightsList()
    }

    fun isLightConnected(lightId: Int, groupId: Int): Boolean =
        if (groupId == NO_GROUP_ID) {
            individualLights.value?.any { it.id == lightId && it.status != LightStatus.DISCONNECTED } ?: false
        } else {
            lightsGroups.value?.any {
                it.id == groupId && (it.status != LightStatus.DISCONNECTED &&
                        it.lightConfiguration.lightsDetails.firstOrNull { l -> l.lightId == lightId && l.status != LightStatus.DISCONNECTED } != null
                        )
            } ?: false
        }


    fun storeLatestScenesList() {
        viewModelScope.launch {
            storeLatestScenes()
        }
    }

    fun getSceneById(sceneId: Int): Scene? = _scenes.value?.firstOrNull { it.id == sceneId }

    fun getScenesList() {
        _scenes.value = getScenes()
    }

    fun getLightsList() {
        individualLights.value = getIndividualLights()
        lightsGroups.value = getLightsGroups()
    }

    fun createNewScene(sceneName: String) {
        _scenes.value = createScene(sceneName)
    }

    fun updateSceneName(sceneId: Int, newSceneName: String) {
        _scenes.value = renameScene(sceneId, newSceneName)
    }

    fun removeScene(sceneId: Int) {
        _scenes.value = deleteScene(sceneId)
    }

    fun applySceneLightsDetails(sceneId: Int) {
        val sceneLightsConfigurations = getSceneById(sceneId)?.lightsConfigurations ?: return
        updateSceneLightsDetails(sceneLightsConfigurations)
    }
}