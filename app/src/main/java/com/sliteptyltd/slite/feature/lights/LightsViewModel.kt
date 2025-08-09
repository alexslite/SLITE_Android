package com.sliteptyltd.slite.feature.lights

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sliteptyltd.slite.data.model.extensions.asOrderedLights
import com.sliteptyltd.slite.data.model.extensions.getGroupContainingLightId
import com.sliteptyltd.slite.data.model.extensions.getGroupStatus
import com.sliteptyltd.slite.data.model.extensions.mapToContainingScenesList
import com.sliteptyltd.slite.data.model.extensions.mapToLightItemsList
import com.sliteptyltd.slite.data.model.extensions.updateLightGroupPowerState
import com.sliteptyltd.slite.data.model.extensions.updateSingleLightPowerState
import com.sliteptyltd.slite.data.model.light.Light
import com.sliteptyltd.slite.data.model.light.GroupLightDetails
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.usecase.DeleteIndividualLightUseCase
import com.sliteptyltd.slite.data.usecase.GetIndividualLightsUseCase
import com.sliteptyltd.slite.data.usecase.GetLightsGroupsUseCase
import com.sliteptyltd.slite.data.usecase.RenameIndividualLightUseCase
import com.sliteptyltd.slite.data.usecase.RenameLightsGroupUseCase
import com.sliteptyltd.slite.data.usecase.UngroupLightsUseCase
import com.sliteptyltd.slite.data.usecase.UpdateLightUseCase
import com.sliteptyltd.slite.data.usecase.UpdateLightsSectionPowerStatusUseCase
import com.sliteptyltd.slite.data.usecase.caching.FetchLightsUseCase
import com.sliteptyltd.slite.data.usecase.caching.StoreLatestLightsListUseCase
import com.sliteptyltd.slite.data.usecase.scenes.CreateSceneUseCase
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.data.usecase.caching.FetchScenesUseCase
import com.sliteptyltd.slite.data.usecase.caching.StoreLatestScenesUseCase
import com.sliteptyltd.slite.data.usecase.scenes.GetContainingScenesUseCase
import com.sliteptyltd.slite.feature.lights.adapter.data.LightUIConfiguration
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsListItem
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType.GROUPS
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType.INDIVIDUAL
import com.sliteptyltd.slite.feature.lights.adapter.data.extensions.isSingleLightItem
import com.sliteptyltd.slite.feature.lights.containingscenes.ContainingScene
import com.sliteptyltd.slite.utils.Constants.Groups.MINIMUM_LIGHTS_COUNT_IN_GROUP
import com.sliteptyltd.slite.utils.Constants.Lights.INDIVIDUAL_LIGHTS_CONTROL_ITEM_ID
import com.sliteptyltd.slite.utils.Constants.Lights.INDIVIDUAL_LIGHTS_HEADER_ID
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHTS_GROUP_CONTROL_ITEM_ID
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHTS_GROUP_HEADER_ID
import com.sliteptyltd.slite.utils.extensions.combineNonNull
import kotlinx.coroutines.launch

class LightsViewModel(
    private val fetchLightsFromLocalStorage: FetchLightsUseCase,
    private val storeLatestLightsList: StoreLatestLightsListUseCase,
    private val getIndividualLights: GetIndividualLightsUseCase,
    private val getLightsGroups: GetLightsGroupsUseCase,
    private val deleteIndividualLight: DeleteIndividualLightUseCase,
    private val renameIndividualLight: RenameIndividualLightUseCase,
    private val renameLightsGroup: RenameLightsGroupUseCase,
    private val ungroupLightsUseCase: UngroupLightsUseCase,
    private val updateLight: UpdateLightUseCase,
    private val updateLightsSectionPowerStatus: UpdateLightsSectionPowerStatusUseCase,
    private val createScene: CreateSceneUseCase,
    private val storeLatestScenes: StoreLatestScenesUseCase,
    private val fetchScenes: FetchScenesUseCase,
    private val getContainingScenes: GetContainingScenesUseCase
) : ViewModel() {

    private val _individualLights = MutableLiveData<List<Light>>(listOf())
    val individualLights: LiveData<List<Light>>
        get() = _individualLights

    private val _lightsGroups = MutableLiveData<List<Light>>(listOf())
    val lightsGroups: LiveData<List<Light>>
        get() = _lightsGroups

    private val _disconnectedLightsAddresses = MediatorLiveData<List<String>>()
    val disconnectedLightsAddresses: LiveData<List<String>>
        get() = _disconnectedLightsAddresses

    private val isIndividualLightsSectionExpanded = MutableLiveData(true)
    private val isLightsGroupsSectionExpanded = MutableLiveData(true)
    val canCreateNewGroup: Boolean get() = getCanCreateGroup()
    val canCreateNewScene: Boolean get() = getCanCreateScene()

    private val _lightsListItems = MediatorLiveData<List<LightsListItem>>()
    val lightsListItems: LiveData<List<LightsListItem>>
        get() = _lightsListItems

    var shouldUpdateListUI = true
    var isLightDetailsConfigurationOngoing = false
    var shouldUpdateIndividualConnections = false
    var shouldUpdateGroupsConnections = false
    var shouldReadLightsInfoAfterUngroup = false

    init {
        _lightsListItems.combineNonNull(
            _individualLights,
            _lightsGroups,
            isIndividualLightsSectionExpanded,
            isLightsGroupsSectionExpanded,
            ::combineToLightsListItems
        )

        _disconnectedLightsAddresses.combineNonNull(
            _individualLights,
            _lightsGroups,
            ::getDisconnectedLightsAddressesList
        )

        fetchLightsList()
        fetchScenesList()
    }

    private fun combineToLightsListItems(
        individualLights: List<Light>,
        lightsGroups: List<Light>,
        isIndividualLightsSectionExpanded: Boolean,
        isLightsGroupsSectionExpanded: Boolean
    ): List<LightsListItem> {
        val displayedLights = mutableListOf<LightsListItem>()
        val currentLights = _lightsListItems.value ?: mutableListOf()

        if (individualLights.isNotEmpty()) {
            displayedLights.add(getLightsHeader(individualLights.size, INDIVIDUAL, isIndividualLightsSectionExpanded))
            if (isIndividualLightsSectionExpanded) {
                val lightsUIConfigurations = mutableMapOf<Int, LightUIConfiguration>()
                currentLights.forEach {
                    if (it is LightsListItem.LightItem && it.isSingleLightItem()) {
                        lightsUIConfigurations[it.itemId] = if (it.status == ON) it.lightUIConfiguration else LightUIConfiguration()
                    }
                }
                displayedLights.addAll(individualLights.asOrderedLights().mapToLightItemsList(lightsUIConfigurations))
            } else {
                displayedLights.add(individualLightsControlItem)
            }
        }

        if (lightsGroups.isNotEmpty()) {
            displayedLights.add(getLightsHeader(lightsGroups.size, GROUPS, isLightsGroupsSectionExpanded))
            if (isLightsGroupsSectionExpanded) {
                displayedLights.addAll(lightsGroups.asOrderedLights().mapToLightItemsList(mapOf()))
            } else {
                displayedLights.add(lightsGroupControlItem)
            }
        }

        return displayedLights
    }

    fun storeLatestLights() {
        viewModelScope.launch {
            storeLatestLightsList()
            storeLatestScenes()
        }
    }

    private fun fetchLightsList() {
        viewModelScope.launch {
            val hasAvailableLights = fetchLightsFromLocalStorage()
            if (hasAvailableLights) {
                shouldUpdateIndividualConnections = true
                _individualLights.value = getIndividualLights()
                shouldUpdateGroupsConnections = true
                _lightsGroups.value = getLightsGroups()
            } else {
                _individualLights.value = emptyList()
                _lightsGroups.value = emptyList()
            }
        }
    }

    private fun fetchScenesList() {
        viewModelScope.launch {
            fetchScenes()
        }
    }

    fun getLightsList() {
        _individualLights.value = getIndividualLights()
        _lightsGroups.value = getLightsGroups()
    }

    fun renameLight(lightId: Int, isLightsGroup: Boolean, newLightName: String) {
        if (isLightsGroup) {
            renameGroup(lightId, newLightName)
        } else {
            renameSingleLight(lightId, newLightName)
        }
    }

    private fun renameSingleLight(lightId: Int, newLightName: String) {
        _individualLights.value = renameIndividualLight(lightId, newLightName)
    }

    private fun renameGroup(groupId: Int, newGroupName: String) {
        _lightsGroups.value = renameLightsGroup(groupId, newGroupName)
    }

    fun getLightById(lightId: Int): Light? {
        val lightLisItem = _lightsListItems.value?.firstOrNull { it.itemId == lightId } ?: return null

        return if (lightLisItem.isSingleLightItem()) {
            _individualLights.value?.firstOrNull { it.id == lightId }
        } else {
            _lightsGroups.value?.firstOrNull { it.id == lightId }
        }
    }

    fun getGroupedLightConfigurationById(lightId: Int): LightConfiguration? =
        _lightsGroups.value?.getGroupContainingLightId(lightId)?.lightConfiguration

    fun getGroupedLightIndividualModeById(lightId: Int): LightConfigurationMode? =
        _lightsGroups.value?.getGroupContainingLightId(lightId)?.lightConfiguration?.lightsDetails?.firstOrNull { it.lightId == lightId }?.mode

    fun getGroupedLightIndividualStatusById(lightId: Int): LightStatus? =
        _lightsGroups.value?.getGroupContainingLightId(lightId)?.lightConfiguration?.lightsDetails?.firstOrNull { it.lightId == lightId }?.status

    fun getGroupedLightGroupId(lightId: Int): Int? =
        _lightsGroups.value?.getGroupContainingLightId(lightId)?.id

    fun deleteLight(lightId: Int) {
        _individualLights.value = deleteIndividualLight(lightId)
    }

    fun disbandLightsGroup(groupId: Int) {
        val isUngroupSuccessful = ungroupLightsUseCase(groupId)
        if (isUngroupSuccessful) {
            getLightsList()
        }
    }

    fun updateLightConfiguration(lightId: Int, lightConfiguration: LightConfiguration) {
        val light = _individualLights.value?.firstOrNull { it.id == lightId }
            ?: _lightsGroups.value?.firstOrNull { it.id == lightId }
            ?: return

        val status = light.status
        val updatedLightConfiguration = if (lightConfiguration.isLightsGroup) {
            val groupedLightsDetails =
                lightConfiguration.lightsDetails.map { it.copy(status = it.status, mode = lightConfiguration.configurationMode) }
            lightConfiguration.copy(lightsDetails = groupedLightsDetails)
        } else {
            lightConfiguration
        }
        updateLight(lightId, updatedLightConfiguration, status)
        shouldUpdateListUI = false
        getLightsList()
        shouldUpdateListUI = true
    }

    fun updateLightUI(lightId: Int, lightConfiguration: LightConfiguration, newLightStatus: LightStatus) {
        updateLight(lightId, lightConfiguration, newLightStatus)
    }

    fun updateLightsGroupUI(updatedLightId: Int, newLightStatus: LightStatus, mode: LightConfigurationMode?) {
        val lightsGroup = _lightsGroups.value?.getGroupContainingLightId(updatedLightId) ?: return
        val groupedLights = mutableListOf<GroupLightDetails>()
        lightsGroup.lightConfiguration.lightsDetails.forEach { light ->
            if (light.lightId == updatedLightId) {
                if (light.status == newLightStatus && light.mode == mode) return
                groupedLights.add(light.copy(status = newLightStatus, mode = mode ?: light.mode))
            } else {
                groupedLights.add(light)
            }
        }
        val updatedGroupConfiguration = lightsGroup.lightConfiguration.copy(lightsDetails = groupedLights)
        updateLight(lightsGroup.id, updatedGroupConfiguration, groupedLights.getGroupStatus())
        getLightsList()
    }

    fun changeLightStatus(lightId: Int, newLightStatus: LightStatus) {
        val light = _individualLights.value?.firstOrNull { it.id == lightId }
            ?: _lightsGroups.value?.firstOrNull { it.id == lightId }
            ?: return

        val isPoweredOn = newLightStatus == ON
        val updatedConfiguration = if (light.lightConfiguration.isIndividualLight) {
            light.updateSingleLightPowerState(isPoweredOn).lightConfiguration
        } else {
            light.updateLightGroupPowerState(isPoweredOn).lightConfiguration
        }

        val isUpdateSuccessful = updateLight(lightId, updatedConfiguration, newLightStatus)
        if (isUpdateSuccessful) {
            getLightsList()
        }
    }

    fun changeSectionPowerState(sectionType: LightsSectionType, isSectionStateOn: Boolean) {
        val isGroupsSection = sectionType == GROUPS
        val updatedList = updateLightsSectionPowerStatus(isGroupsSection, isSectionStateOn)

        if (isGroupsSection) {
            _lightsGroups.value = updatedList
        } else {
            _individualLights.value = updatedList
        }
    }

    private fun getDisconnectedLightsAddressesList(individualLights: List<Light>, lightsGroups: List<Light>): List<String> {
        val individualDisconnectedLights = individualLights.filter { it.status == DISCONNECTED }.mapNotNull { it.address }
        val groupedDisconnectedLights = mutableListOf<String>()
        lightsGroups.forEach { groupedLight ->
            groupedDisconnectedLights.addAll(
                groupedLight.lightConfiguration.lightsDetails.filter { it.status == DISCONNECTED }.mapNotNull { it.address }
            )
        }
        return individualDisconnectedLights + groupedDisconnectedLights
    }

    fun changeSectionDropdownState(sectionType: LightsSectionType, isExpanded: Boolean) {
        if (isExpanded) {
            expandSection(sectionType)
        } else {
            collapseSection(sectionType)
        }
    }

    private fun expandSection(sectionType: LightsSectionType) {
        when (sectionType) {
            INDIVIDUAL -> isIndividualLightsSectionExpanded.value = true
            GROUPS -> isLightsGroupsSectionExpanded.value = true
        }
    }

    private fun collapseSection(sectionType: LightsSectionType) {
        when (sectionType) {
            INDIVIDUAL -> isIndividualLightsSectionExpanded.value = false
            GROUPS -> isLightsGroupsSectionExpanded.value = false
        }
    }

    private fun getLightsHeader(
        sectionSize: Int,
        sectionType: LightsSectionType,
        isExpanded: Boolean
    ): LightsListItem.SectionHeader {
        val headerId = when (sectionType) {
            INDIVIDUAL -> INDIVIDUAL_LIGHTS_HEADER_ID
            GROUPS -> LIGHTS_GROUP_HEADER_ID
        }
        return LightsListItem.SectionHeader(headerId, sectionType, sectionSize, isExpanded)
    }

    private fun getCanCreateGroup(): Boolean {
        val individualLights = _individualLights.value ?: return false
        return individualLights.size >= MINIMUM_LIGHTS_COUNT_IN_GROUP
    }

    private fun getCanCreateScene(): Boolean =
        (_individualLights.value?.any { it.status == ON } == true) ||
                (_lightsGroups.value?.any { it.status == ON } == true)

    fun createNewScene(sceneName: String) {
        createScene(sceneName)
        viewModelScope.launch {
            storeLatestScenes()
        }
    }

    fun getContainingScenesList(lightId: Int): List<ContainingScene> = getContainingScenes(lightId).mapToContainingScenesList()

    companion object {
        private val individualLightsControlItem by lazy { initLightsControlItem(INDIVIDUAL_LIGHTS_CONTROL_ITEM_ID, INDIVIDUAL) }
        private val lightsGroupControlItem by lazy { initLightsControlItem(LIGHTS_GROUP_CONTROL_ITEM_ID, GROUPS) }

        private fun initLightsControlItem(itemId: Int, sectionType: LightsSectionType) =
            LightsListItem.LightsSectionControlItem(itemId, sectionType)
    }
}