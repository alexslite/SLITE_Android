package com.sliteptyltd.slite.views.statelayout

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.utils.ComposableContent
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun StateDetailsLayout(
    stateDetails: StateDetails,
    onActionButtonClickListener: StateLayout.OnActionButtonClickListener? = null,
    loadingStateContent: ComposableContent = {},
    successStateContent: ComposableContent = {}
) {
    SliteTheme {
        when (stateDetails) {
            is StateDetails.Error -> ErrorStateLayout(stateDetails, onActionButtonClickListener)
            StateDetails.Loading -> loadingStateContent()
            StateDetails.Success -> successStateContent()
        }
    }
}

@Preview
@Composable
private fun NoLightsState() {
    StateDetailsLayout(
        StateDetails.Error(
            R.drawable.ic_state_empty,
            R.string.lights_empty_state_title,
            R.string.lights_empty_state_subtitle,
            R.string.onboarding_button_text
        )
    )
}