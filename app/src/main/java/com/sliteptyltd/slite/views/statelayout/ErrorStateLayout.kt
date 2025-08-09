package com.sliteptyltd.slite.views.statelayout

import androidx.annotation.DimenRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.views.composables.MaterialButton

@Composable
fun ErrorStateLayout(
    stateDetails: StateDetails.Error,
    onActionButtonClickListener: StateLayout.OnActionButtonClickListener? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            stateDetails.drawable?.let { stateDrawableRes ->
                Image(painter = painterResource(stateDrawableRes), contentDescription = null)
            }
            stateDetails.title?.let { stateTitleRes ->
                Text(
                    text = stringResource(id = stateTitleRes),
                    style = MaterialTheme.typography.subtitle1,
                    color = colorResource(id = R.color.state_layout_text_color),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.state_layout_title_spacing_top))
                )
            }
            stateDetails.subtitle?.let { stateSubtitleRes ->
                Text(
                    text = stringResource(id = stateSubtitleRes),
                    style = MaterialTheme.typography.body1,
                    color = colorResource(id = R.color.state_layout_text_color),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = dimensionResource(id = getSubtitleSpacingTop(stateDetails.title == null)))
                        .padding(horizontal = dimensionResource(id = R.dimen.state_layout_subtitle_spacing_horizontal))
                )
            }
        }
        stateDetails.buttonText?.let { buttonTextRes ->
            MaterialButton(
                text = stringResource(id = buttonTextRes),
                onClick = { onActionButtonClickListener?.invoke() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.state_layout_button_spacing_horizontal))
                    .padding(bottom = dimensionResource(id = R.dimen.state_layout_button_spacing_bottom))
            )
        }
    }
}

@DimenRes
private fun getSubtitleSpacingTop(isSubtitleAlone: Boolean): Int =
    if (isSubtitleAlone) {
        R.dimen.state_layout_alone_subtitle_spacing_top
    } else {
        R.dimen.state_layout_subtitle_spacing_top
    }