package com.sliteptyltd.slite.feature.updatelights

import android.util.ArrayMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.usecase.GetIndividualLightsUseCase
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.SliteEventSubscriber
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateLightsState(
    var sliteDevices: List<UpdateLightDetails>
)

class UpdateLightsViewModel(
    private val getIndividualLightsUseCase: GetIndividualLightsUseCase,
    private val bluetoothService: BluetoothService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateLightsState(emptyList()))
    val uiState: StateFlow<UpdateLightsState>
        get() = _uiState.asStateFlow()

    private lateinit var serviceSubscriber: SliteEventSubscriber

    private val reconnectionJobs: ArrayMap<String, Job> = ArrayMap()

    fun doOnInit() {
        initListeners()
    }

    fun doOnDestroy() {
        clearListeners()
    }

    fun startUpdateForDevice(address: String) {
        bluetoothService.startUpdateForSubscriber(address)
    }

    fun repopulateList() {
        val listOfLights = getSliteDevices()
        _uiState.update { currentState -> currentState.copy(sliteDevices = listOfLights) }
    }

    private fun initListeners() {
        serviceSubscriber = object : SliteEventSubscriber() {
            override fun onUpdateStateChanged(address: String, updateState: UpdateState) {
                super.onUpdateStateChanged(address, updateState)

                repopulateList()

                if (updateState is UpdateState.Completed) {
                    scheduleReconnection(address)
                }
            }

            override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
                super.onConnectionStateChanged(address, isConnected)

                if (!isConnected) {
                    scheduleReconnection(address)
                } else {
                    reconnectionJobs[address]?.cancel()
                }

                repopulateList()
            }
        }

        getSliteDevices().forEach { light ->
            bluetoothService.addDeviceSubscriber(light.address, serviceSubscriber)
        }
    }

    private fun scheduleReconnection(address: String) {
        reconnectionJobs[address]?.cancel()
        reconnectionJobs[address] = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(UPDATE_RECONNECTION_DELAY_MS)
                bluetoothService.connect(address)
            }
        }
    }

    private fun getSliteDevices(): List<UpdateLightDetails> {
        return getIndividualLightsUseCase.invoke()
            .filter { light ->
                light.status != LightStatus.DISCONNECTED
            }.map { light ->
                val lightAddress = light.address ?: ""
                val updateState = bluetoothService.getUpdateStateForSubscriber(lightAddress)

                UpdateLightDetails(
                    name = light.lightConfiguration.name,
                    address = lightAddress,
                    isConnected = bluetoothService.isSubscriberConnected(lightAddress),
                    updateState = updateState
                )
            }
    }

    private fun clearListeners() {
        getSliteDevices().forEach { light ->
            bluetoothService.removeDeviceSubscriber(light.address, serviceSubscriber)
        }
    }

    companion object {
        private const val UPDATE_RECONNECTION_DELAY_MS = 5000L
    }
}