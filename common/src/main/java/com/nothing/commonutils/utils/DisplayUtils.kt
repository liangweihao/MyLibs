package com.nothing.commonutils.utils

import android.content.Context
import android.content.res.Resources
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.FragmentActivity


public object DisplayUtils {
    fun Float.dp2px(displayMetrics: DisplayMetrics): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics
    )

    fun Float.dp2pxInt(displayMetrics: DisplayMetrics): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics
    ).toInt()

    fun Int.sp2px(displayMetrics: DisplayMetrics): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        displayMetrics
    )


    fun Context.windowDisplayID():Int{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return this.display?.displayId?:0
        }else{
            val systemService: DisplayManager = this.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            return systemService.displays[0].displayId
        }
    }


    fun FragmentActivity.getContentViewSize(): Size {
        val contentView = findViewById<View>(android.R.id.content)
        return Size(contentView.measuredWidth,contentView.measuredHeight)
    }

}