package com.sliteptyltd.slite.feature.effectsservice.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EffectLights(
    val lightsAddresses: List<String>
) : Parcelable