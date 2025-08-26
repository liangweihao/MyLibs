package com.nothing.dnsservice.home

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.nothing.commonutils.utils.BitmapUtils
import com.nothing.commonutils.utils.Lg
import com.nothing.dnsservice.R
import com.nothing.dnsservice.home.router.ScreenShootRouter
import com.nothing.dnsservice.home.vm.MainViewModel
import com.nothing.dnsservice.utils.SizeAuto.adapt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "ScreenShootPreviewScree"

@Composable
fun ScreenShootPreviewScreen(
    viewModel: MainViewModel,
    screenShootRouter: ScreenShootRouter,
    downloadClick: (File) -> Unit
) {
    var progress = remember { mutableIntStateOf(0) }
    // 用于存储加载的图片
    var screenshotBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    var screenTempState = remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current
    val maxHeight = LocalWindowInfo.current.containerSize.height
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val cacheDir = viewModel.getApplication<Application>().cacheDir
            val screenTemp = File(cacheDir, "screen_${System.currentTimeMillis()}.jpg")
            viewModel.takeScreen(screenShootRouter.ip, screenShootRouter.port, screenTemp) {
                Lg.i(TAG, "progress ${it}")
                progress.intValue = it
            }
            // 当下载完成，将图片文件转换为 ImageBitmap
            if (screenTemp.exists()) {
                screenTempState.value = (screenTemp)
                screenshotBitmap.value =
                    BitmapUtils.getSafeBitmap(context, screenTemp.toUri(), maxHeight)
                        ?.asImageBitmap()
                if (screenshotBitmap.value == null) {
                    progress.intValue = -1
                }
            } else {
                progress.intValue = -1
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        if (progress.intValue == -1) {
            Text(text = "远程截图异常", color = Color.Red)
        } else if (progress.intValue < 100 || screenshotBitmap.value == null) {
            CircularProgressIndicator(
                progress = { progress.intValue / 100f }, Modifier.size(50.dp.adapt())
            )
            Text(
                text = "${progress.intValue}",
                color = Color.Green,
                fontSize = TextUnit(24f.dp.adapt().value, TextUnitType.Sp)
            )
        } else {
            Image(
                bitmap = screenshotBitmap.value!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp.adapt()),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_download),
                    modifier = Modifier
                        .size(80.dp.adapt())
                        .background(Color.Gray, shape = RoundedCornerShape(100))
                        .clickable(onClick = {
                            downloadClick(screenTempState.value!!)
                        }),
                    contentDescription = null,
                )
            }

        }
    }

}