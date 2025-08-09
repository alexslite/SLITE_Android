package com.sliteptyltd.slite.feature.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sliteptyltd.slite.data.model.extensions.mapToContainingScenesList
import com.sliteptyltd.slite.data.model.light.Light
import com.sliteptyltd.slite.data.model.scene.Scene
import com.sliteptyltd.slite.data.preference.InternalStorageManager.Companion.NO_NEWLY_CREATED_GROUP_ID
import com.sliteptyltd.slite.data.usecase.CreateGroupUseCase
import com.sliteptyltd.slite.data.usecase.GetIndividualLightsUseCase
import com.sliteptyltd.slite.data.usecase.scenes.GetContainingScenesUseCase
import com.sliteptyltd.slite.feature.lights.containingscenes.ContainingScene

class CreateGroupViewModel(
    getIndividualLights: GetIndividualLightsUseCase,
    private val createGroup: CreateGroupUseCase,
    private val getContainingScenes: GetContainingScenesUseCase
) : ViewModel() {

    private val _availableLights = MutableLiveData<List<AvailableLightItem>>()
    val availableLights: LiveData<List<AvailableLightItem>>
        get() = _availableLights

    init {
        _availableLights.value = getIndividualLights().mapToAvailableLightsItemsList()
    }

    fun createNewGroup(groupName: String) : Int {
        val selectedLights = _availableLights.value.getSelectedLightsIds() ?: return NO_NEWLY_CREATED_GROUP_ID
        return createGroup(groupName, selectedLights)
    }

    fun changeLightSelectionState(lightId: Int) {
        val availableLights = _availableLights.value ?: return
        val updatedLightsList = mutableListOf<AvailableLightItem>()

        availableLights.forEach {
            if (it.id == lightId) {
                updatedLightsList.add(it.copy(isSelected = !it.isSelected))
            } else {
                updatedLightsList.add(it)
            }
        }

        _availableLights.value = updatedLightsList
    }

    fun getScenesContainingGroupLights(): List<ContainingScene> {
        val selectedLights = _availableLights.value.getSelectedLightsIds() ?: return emptyList()
        val containingScenes = mutableListOf<Scene>()
        selectedLights.forEach {
            containingScenes.addAll(getContainingScenes(it))
        }
        return containingScenes.distinctBy { it.id }.mapToContainingScenesList()
    }

    companion object {
        private fun List<Light>.mapToAvailableLightsItemsList(): List<AvailableLightItem> =
            map { it.toAvailableLightItem() }.sortedBy { it.name }

        private fun Light.toAvailableLightItem(): AvailableLightItem =
            AvailableLightItem(id, lightConfiguration.name, false)

        private fun List<AvailableLightItem>?.getSelectedLightsIds(): List<Int>? =
            this?.filter { it.isSelected }?.map { it.id }
    }
}