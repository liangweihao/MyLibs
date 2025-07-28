package com.nothing.dnsservice

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
/**
 *--------------------
 *<p>Author：
 *         lwh
 *<p>Created Time:
 *          2025/7/25
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
object WifiStateUtils {


    fun getCurrentWifiName(context: Context): String {
        // 获取 WifiManager 实例
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // 获取当前连接的 Wi-Fi 信息
        val wifiInfo: WifiInfo =
            wifiManager.connectionInfo
        return wifiInfo.ssid.replace("\"", "")
    }

    fun getCurrentWifiIpAddress(context: Context): String {
        // 获取 WifiManager 实例
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // 获取当前连接的 Wi-Fi 信息
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        // 获取 IP 地址整数形式
        val ipAddress = wifiInfo.ipAddress
        // 将整数形式的 IP 地址转换为点分十进制格式
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }

    fun getCurrentWifiLeve(context: Context): Int {
        // 获取 WifiManager 实例
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // 获取当前连接的 Wi-Fi 信息
        val wifiInfo: WifiInfo =
            wifiManager.connectionInfo
        return wifiManager.calculateSignalLevel(wifiInfo.rssi)
    }

    /**
     * 检查设备是否连接到 Wi-Fi。
     *
     * @param context 上下文对象，用于获取系统服务。
     * @return 如果连接到 Wi-Fi 则返回 true，否则返回 false。
     */
    fun isConnectedToWifi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }
}