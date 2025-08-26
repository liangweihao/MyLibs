package com.nothing.dnsservice.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.nothing.commonutils.utils.Lg
import com.nothing.dnsservice.home.router.InstallAppRouter
import com.nothing.dnsservice.home.vm.MainViewModel
import com.nothing.dnsservice.server.bean.InstalledAppInfo
import com.nothing.dnsservice.server.bean.InstalledAppsResponse
import com.nothing.dnsservice.utils.SizeAuto.adapt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "InstallAppsScreen"

@Composable
fun InstalledAppScreen(viewModel: MainViewModel, router: InstallAppRouter) {

    var installedAppsResponse by remember { mutableStateOf<List<InstalledAppInfo>?>(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO){
            viewModel.fetchInstalledApps(router.ip, router.port).onFailure {
                installedAppsResponse = ArrayList()
            }.onSuccess {
                installedAppsResponse = it.installed_apps
            }
        }

    }
    val context = LocalContext.current

    InstalledAppScreen(installedAppsResponse) { appInfo ->
        // 启动协程执行下载操作
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.downloadApk(
                ip = router.ip,
                port = router.port,
                apkPath = appInfo.app_path,
                progress = {
                    Lg.i(TAG, "下载进度: $it")
                })
                .onSuccess {
                    // 下载成功逻辑，可根据需求修改
                    Lg.i(TAG, "APK 下载成功，路径: ${it.path}")
                    Toast.makeText(context, "下载成功 ${it.path}", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    // 下载失败逻辑，可根据需求修改
                    Lg.e(TAG, "APK 下载失败: ${it.message}")
                    Toast.makeText(context, "下载失败 ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

@Composable
fun InstalledAppScreen(
    deviceInfo: List<InstalledAppInfo>?, onDownloadClick: (InstalledAppInfo) -> Unit
) {
    if (deviceInfo == null) {
        // 数据加载中，显示加载状态
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp.adapt()),
            verticalArrangement = Arrangement.spacedBy(4.dp.adapt()),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                // 显示已安装应用列表
                Text(
                    text = "已安装应用列表", fontWeight = FontWeight.Bold, fontSize = TextUnit(
                        24.dp.adapt().value, TextUnitType.Sp
                    ), modifier = Modifier.padding(top = 4.dp.adapt())
                )
            }
            items(deviceInfo) {
                InstalledAppItem(appInfo = it, onDownloadClick = onDownloadClick)
            }
        }
    }
}

@Composable
fun InstalledAppItem(appInfo: InstalledAppInfo, onDownloadClick: (InstalledAppInfo) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(4.dp.adapt()),
            verticalArrangement = Arrangement.spacedBy(0.dp.adapt())
        ) {
            ProvideTextStyle(
                value = TextStyle(
                    fontSize = TextUnit(
                        24.dp.adapt().value, TextUnitType.Sp
                    )
                )
            ) {
                Text(
                    text = "应用名: ${appInfo.app_name}", fontWeight = FontWeight.Bold
                )
                Text(text = "包名: ${appInfo.package_name}")
                Text(text = "版本名: ${appInfo.version_name}")
                Text(text = "版本号: ${appInfo.version_code}")
                Text(text = "是否系统应用: ${if (appInfo.is_system_app) "是" else "否"}")
                Text(text = "安装路径: ${appInfo.app_path}")
                Button(
                    onClick = {
                        onDownloadClick.invoke(appInfo)
                    }, enabled = appInfo.can_download_apk, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "下载 APK")
                }

            }
        }
    }
}