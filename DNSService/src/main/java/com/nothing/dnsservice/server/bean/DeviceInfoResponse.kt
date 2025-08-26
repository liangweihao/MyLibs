package com.nothing.dnsservice.server.bean

import androidx.annotation.Keep

@Keep
data class InstalledAppInfo(
    val package_name: String,
    val app_name: String,
    val version_name: String,
    val version_code: Long,
    val is_system_app: Boolean,
    val app_path: String,
    var can_download_apk:Boolean
)

@Keep
data class InstalledAppsResponse(
    val installed_apps: List<InstalledAppInfo> = emptyList()
)


@Keep
data class DeviceInfoResponse(
    val brand: String = "",
    val model: String = "",
    val device: String = "",
    val product: String = "",
    val manufacturer: String = "",
    val android_version: String = "",
    val sdk_version: Int = 0,
    val total_internal_storage: Long = 0,
    val available_internal_storage: Long = 0,
    val total_external_storage: Long = 0,
    val available_external_storage: Long = 0,
    val external_storage_available: Boolean = false,
    val total_ram: Long =0,
    val available_ram: Long =0,
    val serial_number: String  = "",

)