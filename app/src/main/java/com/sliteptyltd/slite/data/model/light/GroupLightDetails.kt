package com.sliteptyltd.slite.data.model.light

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GroupLightDetails(
    val lightId: Int,
    val name: String,
    val status: LightStatus,
    val address: String?,
    val mode: LightConfigurationMode
) : Parcelable