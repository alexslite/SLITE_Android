package com.sliteptyltd.slite.feature.addcomponent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun AddComponentDropdown(
    onClose: (() -> Unit)? = null,
    onAddLightClick: (() -> Unit)? = null,
    onNewGroupClick: (() -> Unit)? = null,
    onSaveSceneClick: (() -> Unit)? = null,
    canCreateGroup: Boolean = false,
    canCreateScene: Boolean = false,
    isDropdownOpen: Boolean = false
) {
    AnimatedVisibility(
        visible = isDropdownOpen,
        enter = expandVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = DROPDOWN_SPRING_ANIMATION_DAMPING_RATIO
            )
        )
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            IconButton(
                onClick = { onClose?.invoke() },
                modifier = Modifier.padding(end = dimensionResource(id = R.dimen.add_component_dropdown_close_button_spacing_end)),
            ) {
                Image(painter = painterResource(id = R.drawable.ic_close), contentDescription = null)
            }
            AddComponentButton(
                R.string.add_component_dropdown_light_button_text,
                R.drawable.ic_add,
                R.color.add_component_dropdown_light_background_color,
                true,
                onAddLightClick
            )
            AddComponentButton(
                R.string.add_component_dropdown_group_button_text,
                R.drawable.ic_group,
                R.color.add_component_dropdown_group_background_color,
                canCreateGroup,
                onNewGroupClick
            )
            AddComponentButton(
                R.string.add_component_dropdown_scene_button_text,
                R.drawable.ic_save_scene,
                R.color.add_component_dropdown_scene_background_color,
                canCreateScene,
                onSaveSceneClick
            )
        }
    }
}

@Preview
@Composable
private fun AddComponentOverlay() {
    SliteTheme {
        AddComponentDropdown(
            isDropdownOpen = true
        )
    }
}

private const val DROPDOWN_SPRING_ANIMATION_DAMPING_RATIO = 0.65f