package com.inair.inaircommon.gesture

import android.view.MotionEvent


interface InairOverTouchEventListener {
    fun  dispatchTouchEvent(ev: MotionEvent):Boolean
}

interface OverTouchEventContainer{
    fun setOverTouchEventListener(listener: InairOverTouchEventListener);
}

