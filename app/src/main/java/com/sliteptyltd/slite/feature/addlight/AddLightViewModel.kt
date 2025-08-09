package com.sliteptyltd.slite.feature.addlight

import androidx.lifecycle.ViewModel

class AddLightViewModel: ViewModel() {

    var shouldCheckPermissions: Boolean = true
    var shouldCheckBluetoothAvailability: Boolean = true
    var shouldContinueScanning: Boolean = false
}