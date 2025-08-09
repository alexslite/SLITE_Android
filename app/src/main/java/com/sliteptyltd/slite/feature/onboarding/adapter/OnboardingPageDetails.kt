package com.sliteptyltd.slite.feature.onboarding.adapter

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class OnboardingPageDetails(
    @StringRes
    val title: Int,
    @StringRes
    val description: Int,
    @DrawableRes
    val background: Int,
): Parcelable
