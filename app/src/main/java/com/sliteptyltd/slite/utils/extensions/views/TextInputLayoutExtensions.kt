package com.sliteptyltd.slite.utils.extensions.views

import android.content.res.ColorStateList
import com.google.android.material.textfield.TextInputLayout
import com.sliteptyltd.slite.R

fun TextInputLayout.setOutlineColorForInputStatus(isInputValid: Boolean) {
    val colorRes = if(isInputValid) {
        R.color.edit_text_hint_color
    } else {
        R.color.edit_text_error_color
    }
    val color = resources.getColor(colorRes, null)

    boxStrokeColor = color
    hintTextColor = ColorStateList.valueOf(color)
}