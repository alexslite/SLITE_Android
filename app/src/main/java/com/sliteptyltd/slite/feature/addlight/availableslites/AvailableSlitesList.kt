package com.sliteptyltd.slite.feature.addlight.availableslites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.views.composables.SliteTheme

typealias OnAvailableSliteClick = (defaultSliteName: String, sliteAddress: String) -> Unit

@Composable
fun AvailableSlitesList(
    availableSlites: List<AvailableSliteDetails>,
    onAvailableSliteClick: OnAvailableSliteClick? = null
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.available_slites_list_vertical_spacing_between_items)),
        contentPadding = PaddingValues(
            start = dimensionResource(id = R.dimen.horizontal_guideline),
            end = dimensionResource(id = R.dimen.horizontal_guideline),
            top = dimensionResource(id = R.dimen.vertical_guideline)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        items(availableSlites) { availableSlite ->
            AvailableSlite(availableSlite, onAvailableSliteClick)
        }
    }
}

@Preview
@Composable
private fun AvailableSlitesLayout() {
    SliteTheme {
        AvailableSlitesList(
            listOf(
                AvailableSliteDetails(0, "slite_013336", "AB:10"),
                AvailableSliteDetails(1, "slite_123145", "AB:10"),
                AvailableSliteDetails(2, "slite_133712", "AB:10")
            )
        )
    }
}