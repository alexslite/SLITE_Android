package com.sliteptyltd.slite.utils.extensions.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

val ViewGroup.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)

fun <T : ViewDataBinding> ViewGroup.inflate(@LayoutRes layoutRes: Int): T =
    DataBindingUtil.inflate(layoutInflater, layoutRes, this, false)