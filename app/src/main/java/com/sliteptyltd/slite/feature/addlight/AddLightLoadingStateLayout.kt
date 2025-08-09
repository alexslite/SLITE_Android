package com.sliteptyltd.slite.feature.addlight

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.views.composables.SliteTheme

@Composable
fun AddLightLoadingStateLayout() {
    val lottieAnimationComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.pulse_loading_indicator))

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            LottieAnimation(
                lottieAnimationComposition,
                iterations = LottieConstants.IterateForever,
                clipSpec = LottieClipSpec.Frame(LOADING_ANIMATION_START_FRAME, LOADING_ANIMATION_END_FRAME),
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.add_light_bluetooth_search_loading_indicator_size))
                    .align(Alignment.Center)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_bluetooth),
                modifier = Modifier
                    .size(
                        width = dimensionResource(id = R.dimen.add_light_bluetooth_search_loading_indicator_icon_width),
                        height = dimensionResource(id = R.dimen.add_light_bluetooth_search_loading_indicator_icon_height)
                    )
                    .align(Alignment.Center),
                contentDescription = null
            )
        }
        Text(
            text = stringResource(R.string.add_light_bluetooth_searching_description),
            style = MaterialTheme.typography.body1,
            color = colorResource(id = R.color.text_color)
        )
    }
}

@Preview
@Composable
private fun LoadingScreen() {
    SliteTheme {
        AddLightLoadingStateLayout()
    }
}

private const val LOADING_ANIMATION_START_FRAME = 20
private const val LOADING_ANIMATION_END_FRAME = 100