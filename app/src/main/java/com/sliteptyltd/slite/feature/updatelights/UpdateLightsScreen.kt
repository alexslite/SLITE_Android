package com.sliteptyltd.slite.feature.updatelights

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateState
import com.sliteptyltd.slite.views.composables.SliteTheme

typealias OnUpdateSliteClick = (sliteAddress: String) -> Unit

@Composable
fun UpdateLightsScreen(updateViewModel: UpdateLightsViewModel, onGoBack: () -> Unit) {
    val uiState by updateViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        updateViewModel.repopulateList()
    }

    UpdateLightsScreen(
        uiState = uiState,
        onGoBack = onGoBack,
        onUpdateSliteClick = updateViewModel::startUpdateForDevice
    )
}

@Composable
private fun UpdateLightsScreen(
    uiState: UpdateLightsState,
    onGoBack: () -> Unit,
    onUpdateSliteClick: OnUpdateSliteClick
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(onGoBack)
        SliteDevicesList(sliteDevices = uiState.sliteDevices, onUpdateSliteClick = onUpdateSliteClick)
    }
}

@Composable
private fun TitleBar(onGoBack: () -> Unit) {
    Row {
        Image(
            modifier = Modifier
                .size(48.dp)
                .clickable { onGoBack.invoke() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = null,
            contentScale = ContentScale.Inside
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = stringResource(id = R.string.lights_update_title),
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun SliteDevicesList(sliteDevices: List<UpdateLightDetails>, onUpdateSliteClick: OnUpdateSliteClick) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.available_slites_list_vertical_spacing_between_items)),
        contentPadding = PaddingValues(
            start = dimensionResource(id = R.dimen.horizontal_guideline),
            end = dimensionResource(id = R.dimen.horizontal_guideline),
            top = dimensionResource(id = R.dimen.vertical_guideline)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        items(sliteDevices) { sliteDevice ->
            UpdateLightItem(details = sliteDevice, onUpdateSliteClick = onUpdateSliteClick)
        }
    }
}

@Preview
@Composable
fun Preview() {
    SliteTheme {
        UpdateLightsScreen(
            uiState = UpdateLightsState(
                listOf(
                    UpdateLightDetails(
                        name = "Hello",
                        address = "SomeRandomAddress",
                        isConnected = true,
                        updateState = UpdateState.Completed()
                    )
                )
            ),
            onGoBack = {},
            onUpdateSliteClick = {}
        )
    }
}