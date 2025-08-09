package com.sliteptyltd.slite.feature.addlight.setupslite

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.views.composables.LottieLoadingIndicator
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun SettingUpSliteLayout(isLoading: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            LottieLoadingIndicator()
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_check_mark),
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.setting_up_slite_done_icon_size))
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.red))
                    .padding(dimensionResource(id = R.dimen.setting_up_slite_done_icon_inner_padding)),
                contentDescription = null
            )
        }
        Text(
            text = stringResource(id = setupProgressText(isLoading)),
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.setting_up_slite_progress_text_spacing_top))
        )
    }
}

@StringRes
private fun setupProgressText(isLoading: Boolean): Int =
    if (isLoading) R.string.setting_up_slite_loading_text else R.string.setting_up_slite_done_text

@Preview
@Composable
private fun SettingUpSliteLayout() {
    SliteTheme {
        SettingUpSliteLayout(false)
    }
}