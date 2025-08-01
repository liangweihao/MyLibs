package com.nothing.dnsservice.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


object SizeAuto {

    // 基准屏幕尺寸，可根据实际情况调整
    private val BASE_WIDTH = 810f
    private val BASE_HEIGHT = 1080f

    // 定义一个扩展函数用于全局适配 dp 值
    @Composable
    fun Dp.adapt(): Dp {
        val density = LocalDensity.current
        val containerSize = LocalWindowInfo.current.containerSize
        val screenWidth = with(density) { containerSize.width }
        val screenHeight = with(density) { containerSize.height }

        val widthRatio = screenWidth / (BASE_WIDTH * density.density)
        val heightRatio = screenHeight / (BASE_HEIGHT * density.density)
        val ratio = Math.min(widthRatio, heightRatio)

        return (this.value * ratio).dp
    }

}