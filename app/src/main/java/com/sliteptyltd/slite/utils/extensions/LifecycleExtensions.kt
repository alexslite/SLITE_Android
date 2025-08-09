package com.sliteptyltd.slite.utils.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.RESUMED

val Lifecycle?.isResumed: Boolean get() = this?.currentState?.isAtLeast(RESUMED) ?: false