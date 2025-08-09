package com.sliteptyltd.slite.utils.extensions.views

import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.utils.Constants.Support.URL_CLICKABLE_SPAN_START_INDEX
import com.sliteptyltd.slite.utils.Constants.Support.URL_COLOR_SPAN_START_INDEX
import com.sliteptyltd.slite.utils.extensions.openChooserForSupportEmail

fun TextView.setSupportUrlSpan(supportUrlText: String, url: String) {
    val spannableSupportString = SpannableString(supportUrlText)
    val urlSpan = URLSpan(url)
    val colorSpan = ForegroundColorSpan(resources.getColor(R.color.text_color, null))

    spannableSupportString.setSpan(urlSpan, URL_CLICKABLE_SPAN_START_INDEX, supportUrlText.length, SPAN_INCLUSIVE_INCLUSIVE)
    spannableSupportString.setSpan(colorSpan, URL_COLOR_SPAN_START_INDEX, supportUrlText.length, SPAN_INCLUSIVE_INCLUSIVE)

    movementMethod = LinkMovementMethod.getInstance()
    text = spannableSupportString
}

fun TextView.setOpenEmailSpan(supportUrlText: String, onEmailClientNotFound: () -> Unit) {
    val spannableSupportString = SpannableString(supportUrlText)
    val colorSpan = ForegroundColorSpan(resources.getColor(R.color.text_color, null))

    spannableSupportString.setSpan(
        object : ClickableSpan() {
            override fun onClick(view: View) {
                view.context.openChooserForSupportEmail(onEmailClientNotFound)
            }
        },
        URL_CLICKABLE_SPAN_START_INDEX,
        supportUrlText.length,
        SPAN_INCLUSIVE_INCLUSIVE
    )
    spannableSupportString.setSpan(colorSpan, URL_COLOR_SPAN_START_INDEX, supportUrlText.length, SPAN_INCLUSIVE_INCLUSIVE)

    movementMethod = LinkMovementMethod.getInstance()
    text = spannableSupportString
}

fun TextView.setUpdateFirmwareSpan(supportUrlText: String, onUpdateFirmwareClicked: () -> Unit) {
    val spannableSupportString = SpannableString(supportUrlText)
    val colorSpan = ForegroundColorSpan(resources.getColor(R.color.red, null))

    spannableSupportString.setSpan(
        object : ClickableSpan() {
            override fun onClick(view: View) {
                onUpdateFirmwareClicked.invoke()
            }
        },
        URL_CLICKABLE_SPAN_START_INDEX,
        supportUrlText.length,
        SPAN_INCLUSIVE_INCLUSIVE
    )
    spannableSupportString.setSpan(colorSpan, URL_COLOR_SPAN_START_INDEX, supportUrlText.length, SPAN_INCLUSIVE_INCLUSIVE)

    movementMethod = LinkMovementMethod.getInstance()
    text = spannableSupportString
}