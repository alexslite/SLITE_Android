package com.sliteptyltd.slite.feature.effectsservice

import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BluetoothEffectsPlayer : EffectsPlayer, KoinComponent {

    private val bluetoothService by inject<BluetoothService>()

    override fun setColor(lightAddress: String, color: Int) {
        bluetoothService.setSliteOutputValue(
            lightAddress,
            SliteLightCharacteristic.fromColorInt(color),
            ignoreBlackout = true,
            ignoreWriteOutputNotification = false
        )
    }
}