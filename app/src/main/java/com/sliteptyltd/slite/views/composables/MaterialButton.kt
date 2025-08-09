package com.sliteptyltd.slite.views.composables

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import com.sliteptyltd.slite.R

@Composable
fun MaterialButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: (() -> Unit)? = null) {
    Button(
        onClick = { onClick?.invoke() },
        enabled = enabled,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.button_corner_radius)),
        modifier = modifier
            .height(dimensionResource(id = R.dimen.button_height)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.button_background_color),
            disabledBackgroundColor = colorResource(id = R.color.button_disabled_background_color)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.button,
            color = colorResource(id = R.color.button_text_color)
        )
    }
}