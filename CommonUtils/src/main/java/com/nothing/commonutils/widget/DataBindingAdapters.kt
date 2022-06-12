package com.nothing.commonutils.widget

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.databinding.BindingAdapter

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/6/10
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */

/**
 * 动态绘制带圆角背景
 *
 * @param view
 * @param color
 * @param radius
 */
@BindingAdapter(value = ["bind_view_background_color", "bind_view_background_radius"])
fun setGradient(view:View, color:Int, radius:Float) {
    val gradientDrawable = GradientDrawable()
    gradientDrawable.cornerRadius = radius
    gradientDrawable.setColor(color)
    gradientDrawable.shape = GradientDrawable.RECTANGLE
    view.background = gradientDrawable
}


/**
 * 创建任意圆角的背景
 */
@BindingAdapter(value = ["bind_view_background_color", "bind_view_background_l_t", "bind_view_background_r_t", "bind_view_background_r_b", "bind_view_background_l_b"], requireAll = false)
fun setGradient(view:View, color:Int, lt:Float, rt:Float, rb:Float, lb:Float) {
    val gradientDrawable = GradientDrawable()
    gradientDrawable.cornerRadii = floatArrayOf(lt, lt, rt, rt, rb, rb, lb, lb)
    gradientDrawable.setColor(color)
    gradientDrawable.shape = GradientDrawable.RECTANGLE
    view.background = gradientDrawable
}


/**
 * 带虚线形态的drawable
 *
 * @param view
 * @param color
 * @param strokeColor
 * @param strokeWidth
 * @param radius
 * @param dashWidth
 * @param dashGap
 */
@BindingAdapter(value = ["bind_view_background_color", "bind_view_stroke_width", "bind_view_stroke_color", "bind_view_background_radius", "bind_dash_width", "bind_dash_gap"])
fun setGradient(view:View, color:Int, strokeWidth:Float, strokeColor:Int, radius:Float, dashWidth:Float, dashGap:Float) {
    val gradientDrawable = GradientDrawable()
    gradientDrawable.setColor(color)
    gradientDrawable.shape = GradientDrawable.RECTANGLE
    gradientDrawable.setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
    gradientDrawable.cornerRadius = radius
    view.background = gradientDrawable
}

@BindingAdapter(value = ["bind_view_background_color", "bind_view_stroke_width", "bind_view_stroke_color", "bind_view_background_radius"])
fun setGradient(view:View, color:Int, strokeWidth:Float, strokeColor:Int, radius:Float) {
    val gradientDrawable = GradientDrawable()
    gradientDrawable.setColor(color)
    gradientDrawable.shape = GradientDrawable.RECTANGLE
    gradientDrawable.setStroke(strokeWidth.toInt(), strokeColor)
    gradientDrawable.cornerRadius = radius
    view.background = gradientDrawable
}
