package com.sliteptyltd.slite.feature.groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.utils.Constants.Groups.MINIMUM_LIGHTS_COUNT_IN_GROUP
import com.sliteptyltd.slite.views.composables.MaterialButton
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun AvailableLightsScreen(
    availableLights: List<AvailableLightItem>,
    onLightClick: (lightId: Int) -> Unit,
    onCreateGroupClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.create_group_toolbar_spacing_vertical))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.create_group_back_button_margin_start))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(false),
                        onClick = { onBackPressed() }
                    ),
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.create_group_title),
                style = MaterialTheme.typography.subtitle1,
                color = colorResource(id = R.color.text_color),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.create_group_list_vertical_spacing_between_items)),
            contentPadding = PaddingValues(
                start = dimensionResource(id = R.dimen.horizontal_guideline),
                end = dimensionResource(id = R.dimen.horizontal_guideline),
                top = dimensionResource(id = R.dimen.vertical_guideline)
            ),
            modifier = Modifier.weight(1f)
        ) {
            items(availableLights) { availableLight ->
                AvailableLight(
                    availableLightItem = availableLight,
                    onAvailableLightClick = onLightClick
                )
            }
        }
        AnimatedVisibility(
            visible = availableLights.areEnoughLightsSelectedForNewGroup,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MaterialButton(
                text = stringResource(R.string.create_group_confirm_selection_button_text),
                onClick = { onCreateGroupClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.horizontal_guideline))
                    .padding(bottom = dimensionResource(id = R.dimen.create_group_confirm_selection_button_spacing_bottom))
            )
        }
    }
}

@Preview
@Composable
fun AvailableLightsScreenPreview() {
    SliteTheme {
        Box(modifier = Modifier.background(colorResource(id = R.color.black))) {
            AvailableLightsScreen(
                availableLights = listOf(
                    AvailableLightItem(1, "Office", true),
                    AvailableLightItem(2, "Ambient", false),
                    AvailableLightItem(3, "Room", true)
                ),
                onLightClick = {},
                onCreateGroupClick = {},
                onBackPressed = {}
            )
        }
    }
}

private val List<AvailableLightItem>.areEnoughLightsSelectedForNewGroup get() = count { it.isSelected } >= MINIMUM_LIGHTS_COUNT_IN_GROUP