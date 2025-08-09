package com.sliteptyltd.slite.feature.addcomponent

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun AddComponentButton(
    @StringRes
    textRes: Int,
    @DrawableRes
    iconRes: Int,
    @ColorRes
    backgroundColorRes: Int,
    isEnabled: Boolean,
    onClick: (() -> Unit)? = null
) {
    val alpha = getButtonComponentsAlpha(isEnabled)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.add_component_dropdown_button_height))
            .background(colorResource(id = backgroundColorRes))
            .clickable(isEnabled) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.add_component_dropdown_icon_spacing_horizontal))
                .alpha(alpha),
            contentDescription = null
        )
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier.alpha(alpha),
        )
    }
}

@Preview
@Composable
private fun AddLightsPreview() {
    SliteTheme {
        AddComponentButton(
            R.string.add_component_dropdown_light_button_text,
            R.drawable.ic_add,
            R.color.add_component_dropdown_light_background_color,
            false
        )
    }
}

private fun getButtonComponentsAlpha(isEnabled: Boolean) =
    if (isEnabled) {
        ADD_COMPONENT_BUTTON_ENABLED_ALPHA
    } else {
        ADD_COMPONENT_BUTTON_DISABLED_ALPHA
    }

private const val ADD_COMPONENT_BUTTON_ENABLED_ALPHA = 1f
private const val ADD_COMPONENT_BUTTON_DISABLED_ALPHA = 0.32f