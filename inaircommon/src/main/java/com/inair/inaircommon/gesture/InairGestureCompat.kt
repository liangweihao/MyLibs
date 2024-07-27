package com.inair.inaircommon.gesture

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.ViewConfigurationCompat
import com.inair.inaircommon.GestureListenerImpl


class InairGestureCompat(
    private var context: Context,
    private var interceptEvent: Boolean = false,
    var gestureListenerImpl: GestureListenerImpl
) {
    private val gestureDetectorCompat: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector

    private val TAG = "InairGestureCompat"
    init {
        gestureDetectorCompat = GestureDetector(context, gestureListenerImpl)
        scaleGestureDetector = ScaleGestureDetector(context, gestureListenerImpl)
         gestureDetectorCompat.setIsLongpressEnabled(false)
    }

    var downEvent:MotionEvent? = null
    var upEvent:MotionEvent? = null
    public fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isGestureEnable){
            return false
        }
        gestureDetectorCompat.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP){
            upEvent =  MotionEvent.obtain(event)
            gestureListenerImpl.dispatchEventUP(downEvent!!,upEvent!!)
        }else if (event.action == MotionEvent.ACTION_DOWN){
            downEvent = MotionEvent.obtain(event)
        }
        return interceptEvent
    }

    public fun setIsLongpressEnabled(enable: Boolean) {
        gestureDetectorCompat.setIsLongpressEnabled(enable)
    }

    public fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.source and InputDevice.SOURCE_CLASS_POINTER != 0) {
            if (event.action == MotionEvent.ACTION_SCROLL) {
                val vScroll: Float = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                val hScroll: Float = event.getAxisValue(MotionEvent.AXIS_HSCROLL)

                val delta: Float =  ViewConfigurationCompat.getScaledVerticalScrollFactor(
                    ViewConfiguration.get(context), context
                )
                gestureListenerImpl.onActionScroll(hScroll*delta, vScroll*delta)
            }
        }
        return false
    }


    var isGestureEnable = true
}


@SuppressLint("ClickableViewAccessibility")
fun View.bindGestureCompact(ct: InairGestureCompat) {
    this.isEnabled = true
    this.setOnTouchListener { v, event -> ct.onTouchEvent(event) }
}


fun OverTouchEventContainer.bindOverGestureCompact(ct: InairGestureCompat) {
    this.setOverTouchEventListener(object : InairOverTouchEventListener {
        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            return ct.onTouchEvent(ev)
        }
    })
}