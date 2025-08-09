package com.sliteptyltd.slite.views.composables

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.google.android.material.composethemeadapter.createMdcTheme
import com.sliteptyltd.slite.R

@Composable
fun SliteTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val (colors, _, shapes) = createMdcTheme(
        context = context,
        layoutDirection = layoutDirection
    )
    val typography = sliteTypography()

    if (colors != null && shapes != null) {
        MaterialTheme(
            content = content,
            colors = colors,
            shapes = shapes,
            typography = typography
        )
    } else {
        MaterialTheme(
            content = content,
            typography = typography
        )
    }
}

@Composable
fun sliteTypography(): Typography {
    val helveticaNeue = FontFamily(
        Font(R.font.helvetica_neue_normal),
        Font(R.font.helvetica_neue_medium, FontWeight.Medium),
        Font(R.font.helvetica_neue_bold, FontWeight.Bold)
    )

    return Typography(
        defaultFontFamily = helveticaNeue,
        h1 = TextStyle(
            color = colorResource(id = R.color.text_color),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(id = R.dimen.title_text_size).value.scaledSp(),
            lineHeight = dimensionResource(id = R.dimen.title_line_height).value.scaledSp()
        ),
        subtitle1 = TextStyle(
            color = colorResource(id = R.color.text_color),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(id = R.dimen.subtitle_text_size).value.scaledSp(),
            lineHeight = dimensionResource(id = R.dimen.subtitle_line_height).value.scaledSp()
        ),
        button = TextStyle(
            color = colorResource(id = R.color.text_color),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Medium,
            fontSize = dimensionResource(id = R.dimen.button_text_size).value.scaledSp(),
            lineHeight = dimensionResource(id = R.dimen.button_line_height).value.scaledSp()
        ),
        body1 = TextStyle(
            color = colorResource(id = R.color.text_color),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            fontSize = dimensionResource(id = R.dimen.body_text_size).value.scaledSp(),
            lineHeight = dimensionResource(id = R.dimen.body_line_height).value.scaledSp()
        ),
        caption = TextStyle(
            color = colorResource(id = R.color.text_color),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            fontSize = dimensionResource(id = R.dimen.info_text_size).value.scaledSp(),
            lineHeight = dimensionResource(id = R.dimen.info_line_height).value.scaledSp()
        )
    )
}

@Composable
private fun Float.scaledSp(): TextUnit = (this / LocalDensity.current.fontScale).sp