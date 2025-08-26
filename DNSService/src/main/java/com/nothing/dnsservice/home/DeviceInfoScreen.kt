package com.nothing.dnsservice.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.nothing.commonutils.utils.Lg
import com.nothing.dnsservice.home.router.DeviceInfoRouter
import com.nothing.dnsservice.home.vm.MainViewModel
import com.nothing.dnsservice.server.bean.DeviceInfoResponse
import com.nothing.dnsservice.server.bean.InstalledAppInfo
import com.nothing.dnsservice.utils.SizeAuto.adapt
import com.nothing.dnsservice.utils.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "DeviceInfoScreen"

@Composable
fun DeviceinfoScreen(viewModel: MainViewModel, router: DeviceInfoRouter) {
    var deviceInfoState = remember { mutableStateOf<Result<DeviceInfoResponse>?>(null) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            deviceInfoState.value = viewModel.fetchDeviceInfo(router.ip, router.port)
        }
    }
    if (deviceInfoState.value == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(80.dp.adapt()))
        }
    } else {
        deviceInfoState.value!!.onSuccess { deviceInfoResponse ->
            DeviceInfoScreen(deviceInfoResponse)
        }.onFailure {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "设备信息获取异常", color = Color.Red)
            }
        }
    }
}

@Composable
fun DeviceInfoScreen(deviceInfo: DeviceInfoResponse) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            InfoItem(label = "品牌", value = deviceInfo.brand)
        }
        item {
            InfoItem(label = "型号", value = deviceInfo.model)
        }
        item {
            InfoItem(label = "SN", value = deviceInfo.serial_number)
        }
        item {
            InfoItem(label = "设备", value = deviceInfo.device)
        }
        item {
            InfoItem(label = "产品", value = deviceInfo.product)
        }
        item {
            InfoItem(label = "制造商", value = deviceInfo.manufacturer)
        }
        item {
            InfoItem(label = "安卓版本", value = deviceInfo.android_version)
        }
        item {
            InfoItem(label = "SDK 版本", value = deviceInfo.sdk_version.toString())
        }
        item {
            InfoItem(
                label = "内部存储总量",
                value = formatFileSize(deviceInfo.total_internal_storage)
            )
        }
        item {
            InfoItem(
                label = "可用内部存储",
                value = formatFileSize(deviceInfo.available_internal_storage)
            )
        }
        if (deviceInfo.external_storage_available) {
            item {
                InfoItem(
                    label = "外部存储总量",
                    value = formatFileSize(deviceInfo.total_external_storage)
                )
            }
            item {
                InfoItem(
                    label = "可用外部存储",
                    value = formatFileSize(deviceInfo.available_external_storage)
                )
            }
        }
    }
}



@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label, fontWeight = FontWeight.Bold,
            fontSize = TextUnit(
                24.dp.adapt().value,
                TextUnitType.Sp
            )
        )
        Text(
            text = value, modifier = Modifier.padding(start = 4.dp),
            fontSize = TextUnit(
                24.dp.adapt().value,
                TextUnitType.Sp
            )
        )
    }
}

