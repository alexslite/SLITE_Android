package com.sliteptyltd.slite.views.composables

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sliteptyltd.slite.R

@Composable
fun LottieLoadingIndicator(
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    @RawRes indicatorRes: Int = R.raw.circular_loading_indicator
) {
    val lottieAnimationComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(indicatorRes))

    LottieAnimation(
        lottieAnimationComposition,
        iterations = iterations,
        modifier = modifier.size(dimensionResource(id = R.dimen.loading_indicator_size))
    )
}