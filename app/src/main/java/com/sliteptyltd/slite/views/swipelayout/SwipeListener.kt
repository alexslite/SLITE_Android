package com.sliteptyltd.slite.views.swipelayout

import com.daimajia.swipe.SwipeLayout

open class SwipeListener : SwipeLayout.SwipeListener {

    override fun onStartOpen(layout: SwipeLayout?) = Unit

    override fun onOpen(layout: SwipeLayout?) = Unit

    override fun onStartClose(layout: SwipeLayout?) = Unit

    override fun onClose(layout: SwipeLayout?) = Unit

    override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) = Unit

    override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) = Unit
}