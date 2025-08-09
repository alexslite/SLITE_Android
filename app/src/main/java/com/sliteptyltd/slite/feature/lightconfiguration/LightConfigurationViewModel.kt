package com.sliteptyltd.slite.feature.lightconfiguration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sliteptyltd.slite.data.model.extensions.getGroupStatus
import com.sliteptyltd.slite.data.model.light.GroupLightDetails
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.light.LightStatus.DISCONNECTED
import com.sliteptyltd.slite.data.model.light.LightStatus.OFF
import com.sliteptyltd.slite.data.usecase.GetLightByIdUseCase
import com.sliteptyltd.slite.data.usecase.UpdateLightUseCase
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem
import com.sliteptyltd.slite.utils.color.colorHue

class LightConfigurationViewModel(
    private val selectedLightId: Int,
    private val getLightById: GetLightByIdUseCase,
    private val updateLight: UpdateLightUseCase
) : ViewModel() {

    private val _selectedLightDetails = MutableLiveData<LightConfigurationDetails>()
    val selectedLightDetails: LiveData<LightConfigurationDetails>
        get() = _selectedLightDetails

    var shouldConnect = true

    init {
        getSelectedLight()
    }

    fun updateLightConfiguration(lightConfigurationItem: LightConfigurationItem) {
        val lightDetails = _selectedLightDetails.value ?: return

        updateLight(selectedLightId, lightConfigurationItem.copyConfigurationWithUpdatedValues(lightDetails), lightDetails.status)
        getSelectedLight()
    }

    fun updateLightConfiguration(lightConfiguration: LightConfiguration, status: LightStatus? = null) {
        val lightDetails = _selectedLightDetails.value ?: return

        updateLight(selectedLightId, lightConfiguration, status ?: lightDetails.status)
        getSelectedLight()
    }

    fun updateLightsGroupUI(updatedLightId: Int, newLightStatus: LightStatus?, newMode: LightConfigurationMode?) {
        val lightsGroup = _selectedLightDetails.value ?: return
        val groupedLights = mutableListOf<GroupLightDetails>()
        lightsGroup.configuration.lightsDetails.forEach { light ->
            if (light.lightId == updatedLightId) {
                if (light.status == newLightStatus && light.mode == newMode) return
                groupedLights.add(light.copy(status = newLightStatus ?: light.status, mode = newMode ?: light.mode))
            } else {
                groupedLights.add(light)
            }
        }
        val updatedGroupConfiguration = lightsGroup.configuration.copy(lightsDetails = groupedLights)
        updateLight(lightsGroup.id, updatedGroupConfiguration, groupedLights.getGroupStatus())
        getSelectedLight()
    }

    fun setLightDisconnectedStatus() {
        val lightConfiguration = _selectedLightDetails.value?.configuration ?: return

        updateLight(selectedLightId, lightConfiguration, DISCONNECTED)
        getSelectedLight()
    }

    fun powerOffLight() {
        val lightConfiguration = _selectedLightDetails.value?.configuration ?: return
        val updatedConfiguration = if (lightConfiguration.isIndividualLight) {
            lightConfiguration.copy()
        } else {
            val groupedLights = mutableListOf<GroupLightDetails>()
            lightConfiguration.lightsDetails.forEach { light ->
                if (light.status == DISCONNECTED) {
                    groupedLights.add(light)
                } else {
                    groupedLights.add(light.copy(status = OFF))
                }
            }
            lightConfiguration.copy(lightsDetails = groupedLights)
        }
        updateLight(selectedLightId, updatedConfiguration, OFF)
        getSelectedLight()
    }

    fun updateLightConfigurationMode(selectedConfigurationMode: LightConfigurationMode) {
        val lightDetails = _selectedLightDetails.value ?: return

        updateLight(
            selectedLightId,
            lightDetails.copyWithUpdatedValuesAndClearEffect(configurationMode = selectedConfigurationMode, effect = null),
            lightDetails.status
        )
        getSelectedLight()
    }

    private fun getSelectedLight() {
        getLightById(selectedLightId)?.let { selectedLight ->
            _selectedLightDetails.value = LightConfigurationDetails(
                selectedLightId,
                selectedLight.lightConfiguration,
                selectedLight.status,
                selectedLight.address
            )
        }
    }

    companion object {
        fun LightConfigurationItem.copyConfigurationWithUpdatedValues(lightDetails: LightConfigurationDetails): LightConfiguration =
            when (this) {
                is LightConfigurationItem.BrightnessConfigurationItem -> lightDetails.copyWithUpdatedValues(brightness = brightness)
                is LightConfigurationItem.TemperatureConfigurationItem -> lightDetails.copyWithUpdatedValues(temperature = temperature)
                is LightConfigurationItem.ColorConfigurationItem -> lightDetails.copyWithUpdatedValues(
                    saturation = saturation,
                    hue = color.colorHue
                )
                is LightConfigurationItem.EffectsConfigurationItem -> lightDetails.copyWithUpdatedValues(effect = effect)
            }

        private fun LightConfigurationDetails.copyWithUpdatedValues(
            hue: Float? = null,
            brightness: Int? = null,
            temperature: Int? = null,
            saturation: Int? = null,
            effect: Effect? = null,
            configurationMode: LightConfigurationMode? = null
        ): LightConfiguration = configuration.copy(
            hue = hue ?: configuration.hue,
            brightness = brightness ?: configuration.brightness,
            temperature = temperature ?: configuration.temperature,
            saturation = saturation ?: configuration.saturation,
            effect = effect ?: configuration.effect,
            configurationMode = configurationMode ?: configuration.configurationMode,
            lightsDetails = configuration.lightsDetails.map {
                it.copy(
                    mode = configurationMode ?: configuration.configurationMode,
                    status = it.determineUpdatedStatus((configurationMode ?: configuration.configurationMode), status)
                )
            }
        )

        private fun LightConfigurationDetails.copyWithUpdatedValuesAndClearEffect(
            hue: Float? = null,
            brightness: Int? = null,
            temperature: Int? = null,
            saturation: Int? = null,
            effect: Effect?,
            configurationMode: LightConfigurationMode? = null
        ): LightConfiguration = configuration.copy(
            hue = hue ?: configuration.hue,
            brightness = brightness ?: configuration.brightness,
            temperature = temperature ?: configuration.temperature,
            saturation = saturation ?: configuration.saturation,
            effect = effect,
            configurationMode = configurationMode ?: configuration.configurationMode,
            lightsDetails = configuration.lightsDetails.map {
                it.copy(
                    mode = configurationMode ?: configuration.configurationMode,
                    status = it.determineUpdatedStatus((configurationMode ?: configuration.configurationMode), status)
                )
            }
        )

        private fun GroupLightDetails.determineUpdatedStatus(
            newConfigurationMode: LightConfigurationMode,
            groupStatus: LightStatus
        ): LightStatus = if ((newConfigurationMode == EFFECTS) || status == DISCONNECTED) status else groupStatus
    }
}