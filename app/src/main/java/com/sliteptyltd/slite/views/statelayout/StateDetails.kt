package com.sliteptyltd.slite.views.statelayout

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed interface StateDetails {

    object Success : StateDetails

    object Loading : StateDetails

    data class Error(
        @DrawableRes
        val drawable: Int? = null,
        @StringRes
        val title: Int? = null,
        @StringRes
        val subtitle: Int? = null,
        @StringRes
        val buttonText: Int? = null
    ) : StateDetails
}
