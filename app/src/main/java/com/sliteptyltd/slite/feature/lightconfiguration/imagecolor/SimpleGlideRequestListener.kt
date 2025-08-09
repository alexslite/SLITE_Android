package com.sliteptyltd.slite.feature.lightconfiguration.imagecolor

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

abstract class SimpleGlideRequestListener : RequestListener<Drawable> {

    final override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean =
        onLoadFailed()

    final override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean = onResourceReady(resource)

    abstract fun onLoadFailed(): Boolean

    abstract fun onResourceReady(resource: Drawable?): Boolean
}