package com.sliteptyltd.slite.feature.addlight.availableslites

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
fun AvailableSlite(
    availableSliteDetails: AvailableSliteDetails,
    onAvailableSliteClick: OnAvailableSliteClick? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.available_slites_list_item_height))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.available_slites_list_item_corner_radius)))
            .background(colorResource(id = R.color.add_light_available_slite_item_background))
            .clickable { onAvailableSliteClick?.invoke(availableSliteDetails.name, availableSliteDetails.address) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = availableSliteDetails.name,
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.available_slites_list_item_text_spacing_start))
        )
        Image(
            painter = painterResource(id = R.drawable.ic_available_slite_arrow),
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.available_slites_list_item_arrow_spacing_end)),
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun AvailableSliteLayout() {
    SliteTheme {
        AvailableSlite(AvailableSliteDetails(0, "slite_013336", "AB:10"))
    }
}