package com.nothing.dnsservice.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.nothing.commonutils.utils.Lg
import com.nothing.dnsservice.App
import com.nothing.dnsservice.WifiStateUtils
import com.nothing.dnsservice.utils.SizeAuto.adapt
import javax.jmdns.ServiceInfo

/**
 *--------------------
 *<p>Author：
 *         lwh
 *<p>Created Time:
 *          2025/7/29
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
 * 主界面组件，展示网络地址信息和服务解析信息列表。
 * @param inetAddresses 网络地址信息列表
 * @param serviceInfos 服务解析信息列表
 */
@Composable
fun MainScreen(
    inetAddresses: List<String>,
    serviceInfos: List<ServiceInfo>,
    onPrivateStorageClick: (ServiceInfo) -> Unit,
    onExternalStorageClick: (ServiceInfo) -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp.adapt())
        ) {
            // 显示网络地址信息
            Text(
                text = "网络结构信息",
                style = MaterialTheme.typography.titleLarge,
                fontSize = TextUnit(24.dp.adapt().value, TextUnitType.Sp),
                modifier = Modifier.padding(bottom = 8.dp.adapt())
            )
            Text(
                text = "${WifiStateUtils.getCurrentWifiName(App.context)}(${
                    WifiStateUtils.getCurrentWifiIpAddress(
                        App.context
                    )
                })(${WifiStateUtils.getCurrentWifiLeve(App.context)})"
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 16.dp.adapt())
            ) {
                items(inetAddresses) { address ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp.adapt())
                    ) {
                        Text(
                            text = address, modifier = Modifier.padding(8.dp.adapt())
                        )
                    }
                }
            }
            // 显示服务解析信息
            Text(
                text = "设备信息",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp.adapt())
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(serviceInfos) { info ->
                    // 使用 val 声明，避免不必要的重新赋值
                    val loading = remember { mutableStateOf(false) }
                    // 打印初始连接时间状态，修正为打印 connectTimeMs.value
                    ServiceInfoItem(
                        title = info.name,
                        des = "${info.hostAddresses.firstOrNull() ?: "Unknown"}:${info.port}",
                        // 使用派生状态的值
                        privateStorage = "内部",
                        privateStorageClick = { onPrivateStorageClick(info) },
                        externalStorage = "外部",
                        externalStorageClick = { onExternalStorageClick(info) }

                    )
                }
            }
        }
    }
}

private const val TAG = "MainActivity"

/**
 * 服务信息项组件，展示单个服务的类型、名称、IP 地址和端口信息。
 * @param info 服务信息对象
 */
@Composable
fun ServiceInfoItem(
    title: String, des: String,
    privateStorage: String, privateStorageClick: () -> Unit,
    externalStorage: String, externalStorageClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp.adapt())
    ) {
        Box(
            modifier = Modifier.padding(8.dp.adapt())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(text = title)
                    Text(text = des)
                }
                Box(
                    modifier = Modifier
                        .background(Color.Blue, shape = RoundedCornerShape(18.dp))
                        .padding(16.dp.adapt(), 8.dp.adapt())
                        .clickable {},
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = privateStorage,
                        color = Color.White,
                        fontSize = TextUnit(24.dp.adapt().value, TextUnitType.Sp),
                        modifier = Modifier.clickable(onClick = privateStorageClick)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp.adapt())
                        .background(Color.Blue, shape = RoundedCornerShape(18.dp))
                        .padding(16.dp.adapt(), 8.dp.adapt())
                        .clickable {},
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = externalStorage,
                        color = Color.White,
                        fontSize = TextUnit(24.dp.adapt().value, TextUnitType.Sp),
                        modifier = Modifier.clickable(onClick = externalStorageClick)
                    )
                }

            }
        }
    }
}

