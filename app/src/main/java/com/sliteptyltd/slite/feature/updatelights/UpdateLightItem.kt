package com.sliteptyltd.slite.feature.updatelights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.version.Version
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateState
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun UpdateLightItem(details: UpdateLightDetails, onUpdateSliteClick: OnUpdateSliteClick) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(id = R.color.dark_grey))
            .padding(dimensionResource(id = R.dimen.spacing_delta))
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = details.name,
                style = MaterialTheme.typography.subtitle1
            )

            if (details.isConnected) {
                Text(
                    text = stringResource(id = R.string.lights_update_firmware_version, details.updateState.currentVersion),
                    style = MaterialTheme.typography.caption,
                )
            }
        }
        UpdateArea(
            modifier = Modifier.align(Alignment.CenterVertically),
            details = details,
            onUpdateSliteClick = onUpdateSliteClick
        )
    }
}

@Composable
private fun UpdateArea(modifier: Modifier, details: UpdateLightDetails, onUpdateSliteClick: OnUpdateSliteClick) {
    Box(modifier = modifier) {
        if (details.isConnected && details.updateState is UpdateState.NotAvailable) {
            Text(
                text = stringResource(id = R.string.lights_update_latest_version_installed),
                style = MaterialTheme.typography.caption,
                color = colorResource(id = R.color.grey)
            )
        } else {
            val buttonEnabled = details.updateState is UpdateState.Available && details.isConnected
            val buttonLabel = if (details.isConnected) {
                when (details.updateState) {
                    is UpdateState.InProgress -> stringResource(
                        id = R.string.lights_update_progress_percentage,
                        details.updateState.progress
                    )
                    is UpdateState.Available -> stringResource(id = R.string.lights_update_update_label)
                    else -> stringResource(id = R.string.lights_update_done_label)
                }
            } else {
                stringResource(id = R.string.lights_list_light_item_status_disconnected_text)
            }

            Button(modifier = Modifier.sizeIn(minHeight = dimensionResource(id = R.dimen.button_height)),
                enabled = buttonEnabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.red),
                    disabledBackgroundColor = colorResource(id = R.color.grey)
                ),
                shape = MaterialTheme.shapes.medium,
                onClick = { onUpdateSliteClick(details.address) }
            ) {
                Text(
                    text = buttonLabel,
                    style = MaterialTheme.typography.button
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewUpdateLightItem() {
    SliteTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            UpdateLightItem(
                details = UpdateLightDetails(
                    name = "Office",
                    address = "SomeRandomAddress",
                    isConnected = false,
                    updateState = UpdateState.InProgress(Version.parseFromString("2.5.0"), 20)
                ),
                onUpdateSliteClick = {}
            )
        }
    }
}