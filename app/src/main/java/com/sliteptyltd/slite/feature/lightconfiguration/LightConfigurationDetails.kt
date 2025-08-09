package com.sliteptyltd.slite.feature.lightconfiguration

import android.os.Parcelable
import androidx.annotation.Keep
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LightConfigurationDetails(
    val id: Int,
    val configuration: LightConfiguration,
    val status: LightStatus,
    val address: String?
) : Parcelable