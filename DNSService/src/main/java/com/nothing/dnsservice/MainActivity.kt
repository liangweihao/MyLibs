package com.nothing.dnsservice

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.nothing.commonutils.utils.Lg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import javax.jmdns.JmDnsUtils
import javax.jmdns.NetworkTopologyEvent
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    // 用于存储网络地址信息
    private val inetAddresses = mutableStateListOf<String>()

    // 用于存储服务解析信息
    private val serviceInfos = mutableStateListOf<ServiceInfo>()

    private val dnsCallback = object : JmDnsUtils.JmDnsDiscoveryCallback {
        override fun onServiceAdded(event: ServiceEvent) {}

        override fun onServiceRemoved(event: ServiceEvent) {}

        override fun onServiceResolved(event: ServiceEvent) {

            lifecycleScope.launch(Dispatchers.Main) {
                if (event.info.inet4Addresses.isNotEmpty()) {
                    serviceInfos.addUnique(event.info)
                }
            }
        }

        override fun onInetAddressAdded(event: NetworkTopologyEvent) {
            lifecycleScope.launch(Dispatchers.Main) {
                inetAddresses.add(event.inetAddress.hostAddress ?: "")
            }
        }

        override fun onInetAddressRemoved(event: NetworkTopologyEvent) {
            lifecycleScope.launch(Dispatchers.Main) {
                event.inetAddress.hostAddress?.let { inetAddresses.remove(it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            JmDnsUtils.discoverDefaultServices(dnsCallback)
        }
        setContent {
            MainScreen(inetAddresses.map { it }, serviceInfos, { info ->
                val ip = info.hostAddresses.firstOrNull()
                val port = info.port
                if (ip != null) {
                    var url = HttpUrl.Builder().scheme("http").host(ip).port(port)
                        .addEncodedPathSegment("file_data").build()
                    lifecycleScope.launch(Dispatchers.IO) {
                        fetchData(url).onFailure {
                            Lg.e(TAG, "onFailure: $it")

                        }.onSuccess {
                            Lg.i(TAG, "onResponse: $it")
                        }
                    }

                } else {


                }
            })
        }
    }

    private suspend fun fetchData(url: HttpUrl): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            responseData ?: throw IOException("${url}:Response body is null")
        }
    }

}

fun MutableList<ServiceInfo>.addUnique(info: ServiceInfo) {
    if (!info.name.contains(JmDnsUtils.SERVICE_NAME_SUFFIX)) {
        return
    }
    val exists = any {
        it.hostAddresses.firstOrNull() == info.hostAddresses.firstOrNull() && it.port == info.port

    }
    if (!exists) {
        add(info)
    }
}


/**
 * 主界面组件，展示网络地址信息和服务解析信息列表。
 * @param inetAddresses 网络地址信息列表
 * @param serviceInfos 服务解析信息列表
 */
@Composable
fun MainScreen(
    inetAddresses: List<String>, serviceInfos: List<ServiceInfo>, test: (ServiceInfo) -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 显示网络地址信息
            Text(
                text = "网络结构信息",
                style = MaterialTheme.typography.titleLarge,
                fontSize = TextUnit(24.dp.value, TextUnitType.Sp),
                modifier = Modifier.padding(bottom = 8.dp)
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
                    .padding(bottom = 16.dp)
            ) {
                items(inetAddresses) { address ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = address, modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            // 显示服务解析信息
            Text(
                text = "设备信息",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(serviceInfos) { info ->
                    var connectTimeMs =
                    ServiceInfoItem(
                        title = info.name,
                        des = "${info.hostAddresses.firstOrNull() ?: "Unknown"}:${info.port}",
                        button = "连接(ms)",
                        test = { test(info) })
                }
            }
        }
    }
}

/**
 * 服务信息项组件，展示单个服务的类型、名称、IP 地址和端口信息。
 * @param info 服务信息对象
 */
@Composable
fun ServiceInfoItem(title: String, des: String, button: String, test: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.padding(8.dp)
        ) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(text = title)
                    Text(text = des)
                }
                Box(
                    modifier = Modifier
                        .background(Color.Blue, shape = RoundedCornerShape(18.dp))
                        .padding(16.dp, 8.dp)
                        .clickable {}) {
                    Text(
                        text = button,
                        color = Color.White,
                        modifier = Modifier.clickable(onClick = test)
                    )
                }

            }
        }
    }
}
