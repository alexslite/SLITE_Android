package com.sliteptyltd.slite.feature.updatelights

import com.sliteptyltd.slite.utils.bluetooth.update.UpdateState

data class UpdateLightDetails(
    val name: String,
    val address: String,
    val isConnected: Boolean,
    val updateState: UpdateState
)
