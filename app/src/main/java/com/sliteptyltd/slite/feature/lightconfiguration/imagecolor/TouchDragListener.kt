package com.sliteptyltd.slite.feature.lightconfiguration.imagecolor

import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.MotionEvent.ACTION_UP
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.View

class TouchDragListener(
    private val onActionMoveListener: (dx: Float, dy: Float, event: MotionEvent?) -> Unit,
    private val onActionDownListener: ((event: MotionEvent?) -> Unit)? = null,
    private val onActionUpListener: ((event: MotionEvent?) -> Unit)? = null
) : View.OnTouchListener {

    private var activeCrosshairPointerId = INVALID_POINTER_ID
    private var lastTouchX = DEFAULT_TOUCH_COORDINATE_VALUE
    private var lastTouchY = DEFAULT_TOUCH_COORDINATE_VALUE

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        view?.performClick()
        event ?: return false

        when (event.actionMasked) {
            ACTION_DOWN -> {
                val pointerIndex = event.actionIndex
                lastTouchX = event.getX(pointerIndex)
                lastTouchY = event.getY(pointerIndex)
                activeCrosshairPointerId = event.getPointerId(DEFAULT_TOUCH_POINTER_INDEX)

                onActionDownListener?.invoke(event)
            }
            ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activeCrosshairPointerId)
                val dx = event.getX(pointerIndex) - lastTouchX
                val dy = event.getY(pointerIndex) - lastTouchY

                onActionMoveListener(dx, dy, event)
            }
            ACTION_UP -> {
                activeCrosshairPointerId = INVALID_POINTER_ID

                onActionUpListener?.invoke(event)
            }
            ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                if (event.getPointerId(pointerIndex) == activeCrosshairPointerId) {
                    val newPointerIndex = if (pointerIndex == DEFAULT_TOUCH_POINTER_INDEX) {
                        UPDATED_TOUCH_POINTER_INDEX
                    } else {
                        DEFAULT_TOUCH_POINTER_INDEX
                    }
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activeCrosshairPointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    companion object {
        private const val UPDATED_TOUCH_POINTER_INDEX = 1
        private const val DEFAULT_TOUCH_POINTER_INDEX = 0
        private const val DEFAULT_TOUCH_COORDINATE_VALUE = 0f
    }
}