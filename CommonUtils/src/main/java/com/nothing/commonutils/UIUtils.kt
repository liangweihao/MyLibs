package com.nothing.commonutils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/10
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */


fun ViewGroup.inflate(layoutid:Int, useParenParams:Boolean = false, attachParent:Boolean = false):View {
    return LayoutInflater.from(this.context)
        .inflate(layoutid, if (useParenParams) this else null, attachParent)
}