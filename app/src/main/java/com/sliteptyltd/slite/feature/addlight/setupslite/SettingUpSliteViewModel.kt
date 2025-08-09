package com.sliteptyltd.slite.feature.addlight.setupslite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.usecase.AddIndividualLightUseCase
import com.sliteptyltd.slite.data.usecase.caching.StoreLatestLightsListUseCase
import kotlinx.coroutines.launch

class SettingUpSliteViewModel(
    private val addIndividualLight: AddIndividualLightUseCase,
    private val storeLatestLightsList: StoreLatestLightsListUseCase
) : ViewModel() {

    fun addLight(address: String, lightConfiguration: LightConfiguration, status: LightStatus) {
        viewModelScope.launch {
            addIndividualLight(address.hashCode(), address, lightConfiguration, status)
            storeLatestLightsList()
        }
    }
}