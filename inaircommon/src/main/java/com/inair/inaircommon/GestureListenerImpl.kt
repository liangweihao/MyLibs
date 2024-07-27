package com.inair.inaircommon


import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.TOOL_TYPE_FINGER
import android.view.MotionEvent.TOOL_TYPE_MOUSE
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration

open class GestureListenerImpl : GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener {

    protected var flingEnable = false
        set(value) {
            field = value
        }

    protected var canScroll = false
        set(value) {
            field = value
        }
    private val TAG = "GestureListenerImpl"
    override fun onDown(e: MotionEvent): Boolean {
        swipeStartTime = System.currentTimeMillis()
        isScrolling = false
        canScroll = true
        return true
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {

        return false
    }
    // 优先响应缩放事件
    public var isScaleing = false

    public var isScrolling = false

    private var lastScrollSpan = Float.MAX_VALUE

    protected var scrollDSpan: Float = 0f

    /**
     *
     *
     * @param distanceY 大于0 向上滑动（缩小） 小于0向下滑动(放大)
     * @param distanceX 大于0 向左滑动 小于0向右滑动
     * */
    override fun onScroll(
        downEvent: MotionEvent, moveEvent: MotionEvent, distanceX: Float, distanceY: Float
    ): Boolean {
        val cSpan = getSpan(downEvent.x, downEvent.y, moveEvent.x, moveEvent.y)
        if (lastScrollSpan == Float.MAX_VALUE) {
            lastScrollSpan = cSpan
        } else {
            scrollDSpan = cSpan - lastScrollSpan
        }
        if (!isScrolling && canScroll) {
            isScrolling = true
            onScrollStart()
        }
        return true
    }


    protected fun isMoveTRight(value: Float): Boolean {
        if (value > 0){
            return true
        }
        return false
    }

    protected fun isMoveTLeft(value: Float):Boolean{
        if (value < 0){
            return true
        }
        return false
    }
    protected fun isMoveTDown(value: Float): Boolean {
        if (value < 0f) {
            return true
        }
        return false
    }

    protected fun isMoveTUp(value: Float): Boolean {
        if (value > 0f) {
            return true
        }
        return false
    }


    protected fun isScaleBig(value: Float): Boolean {
        if (value < 0f) {
            return true
        }
        return false
    }


    protected fun isScaleSmail(value: Float): Boolean {
        if (value > 0f) {
            return true
        }
        return false
    }

    protected fun isMouseAction(event: MotionEvent): Boolean {
        return event.getToolType(0) == TOOL_TYPE_MOUSE
    }

    protected fun isTouchAction(event: MotionEvent): Boolean {
        return event.getToolType(0) == TOOL_TYPE_FINGER
    }


    protected fun getSpan(downX: Float, downY: Float, moveX: Float, moveY: Float): Float {
        var focusX = downX
        var focusY = downY
        var devSumX = Math.abs(moveX - focusX)
        var devSumY = Math.abs(moveY - focusY)
        val devX: Float = devSumX
        val devY: Float = devSumY
        val spanX: Float = devX * 2
        val spanY: Float = devY * 2
        return Math.hypot(spanX.toDouble(), spanY.toDouble()).toFloat()
    }

    override fun onLongPress(e: MotionEvent) {
    }


    override fun onFling(
        downEvent: MotionEvent, upEvent: MotionEvent, velocityX: Float, velocityY: Float
    ): Boolean {
        return flingEnable
    }

    fun dispatchEventUP(downEvent: MotionEvent, upEvent: MotionEvent){
        if (!flingEnable) {
            if (isScrolling) {
                isScrolling = false
                onScrollEnd(downEvent, upEvent)
            }
        }
    }
    private var swipeStartTime= 0L

    protected open fun onScrollStart() {

    }

    protected open fun performSwipeTRight(){

    }

    protected open fun performSwipeTLeft(){

    }

    protected open fun onScrollEnd(downEvent: MotionEvent, upEvent: MotionEvent) {
         if(System.currentTimeMillis() - swipeStartTime <= ViewConfiguration.getLongPressTimeout()) {
            val atan2 = Math.atan2(
                Math.abs(upEvent.y - downEvent.y).toDouble(),
                Math.abs(upEvent.x - downEvent.x).toDouble()
            )
//                                LogUtils.d(TAG, "onScrollEnd: ${Math.toDegrees(atan2)} -> ${childBinding.photoView.attacher.enterOffsetMode}")

            if (Math.toDegrees(atan2) < 45) {
                if (isMoveTRight(upEvent.x - downEvent.x) && Math.abs(upEvent.x - downEvent.x) > 10) {
                    performSwipeTRight()
                } else if (isMoveTLeft(upEvent.x - downEvent.x) && Math.abs(
                        upEvent.x - downEvent.x
                    ) > 10
                ) {
                    performSwipeTLeft()
                }
            }
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        // TODO:LWH  2023/10/24 目前inair 会发出scroll 然后 scale的连带事件 这里为了屏蔽事件 在产生scale的时候 中断scroll的行为
        isScrolling = false
        canScroll =false
        isScaleing = true
        return true
    }


    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        isScaleing = true
        return true

    }

    open fun onActionScroll(hScroll: Float, vScroll: Float) {

    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        isScaleing = false
    }
}