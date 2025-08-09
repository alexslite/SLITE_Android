package com.sliteptyltd.slite.feature.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun AvailableLight(
    availableLightItem: AvailableLightItem,
    onAvailableLightClick: (lightId: Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.create_group_light_item_height))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.create_group_light_item_corner_radius)))
            .background(colorResource(id = R.color.create_group_light_item_background))
            .clickable { onAvailableLightClick(availableLightItem.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = getLightSelectedStateImageResource(availableLightItem.isSelected)),
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.horizontal_guideline)),
            contentDescription = null
        )
        Text(
            text = availableLightItem.name,
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.create_group_light_item_name_margin_start))
        )
    }
}

@Preview
@Composable
private fun AvailableLightPreview() {
    SliteTheme {
        AvailableLight(
            availableLightItem = AvailableLightItem(1, "Office", true),
            onAvailableLightClick = {}
        )
    }
}

private fun getLightSelectedStateImageResource(isSelected: Boolean): Int =
    if (isSelected) {
        R.drawable.ic_checkmark
    } else {
        R.drawable.ic_available_light_unchecked
    }