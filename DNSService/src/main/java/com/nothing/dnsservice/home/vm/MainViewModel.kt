package com.nothing.dnsservice.home.vm

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.dnsservice.server.RetrofitClient
import com.nothing.dnsservice.server.bean.AppDataResponse
import com.nothing.dnsservice.server.bean.DeviceInfoResponse
import com.nothing.dnsservice.server.bean.InstalledAppsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.OutputStream
import javax.jmdns.JmDnsUtils
import javax.jmdns.NetworkTopologyEvent
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import kotlin.jvm.Throws

// 定义新的数据类，用于存储 ServiceInfo 和设备信息
data class ServiceDisplayInfo(
    val serviceInfo: ServiceInfo,
    val deviceInfo: DeviceInfoResponse?,
    val isOnline: Boolean
)


public class MainViewModel(application: Application) : AndroidViewModel(application) {

    // 用于存储网络地址信息
    val inetAddresses = mutableStateListOf<String>()

    // 用于存储服务解析信息
    val serviceInfos = mutableStateListOf<ServiceDisplayInfo>()


    // 定义 Mutex 用于同步对 serviceInfos 的访问
    private val serviceInfosMutex = Mutex()

    private var checkOnlineJob: Job? = null

    private val dnsCallback = object : JmDnsUtils.JmDnsDiscoveryCallback {
        override fun onServiceAdded(event: ServiceEvent) {}

        override fun onServiceRemoved(event: ServiceEvent) {
            viewModelScope.launch(Dispatchers.IO) {
                serviceInfosMutex.withLock {
                    serviceInfos.removeIf { it.serviceInfo.name == event.info.name }
                }
            }
        }

        override fun onServiceResolved(event: ServiceEvent) {
            viewModelScope.launch(Dispatchers.IO) {
                serviceInfosMutex.withLock {
                    if (event.info.inet4Addresses.isNotEmpty()) {
                        // 初始时设备信息为 null，状态为未知，等待检测
                        val newService = ServiceDisplayInfo(
                            event.info,
                            null,
                            false
                        )
                        serviceInfos.addUnique(newService)
                    }
                }
            }
        }

        override fun onInetAddressAdded(event: NetworkTopologyEvent) {
            viewModelScope.launch(Dispatchers.Main) {
                inetAddresses.add(event.inetAddress.hostAddress ?: "")
            }
        }

        override fun onInetAddressRemoved(event: NetworkTopologyEvent) {
            viewModelScope.launch(Dispatchers.Main) {
                event.inetAddress.hostAddress?.let { inetAddresses.remove(it) }
            }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val jmmDNS = JmDnsUtils.discoverDefaultServices(dnsCallback) ?: return@launch
            startOnlineCheck()
        }
    }

    private fun startOnlineCheck() {
        checkOnlineJob = viewModelScope.launch {
            while (isActive) {
                delay(5000) // 每 5 秒检测一次
                val offlineServices = mutableListOf<ServiceDisplayInfo>()
                serviceInfosMutex.withLock {
                    serviceInfos.forEach { serviceDisplayInfo ->
                        val responseResult = isServiceOnline(serviceDisplayInfo.serviceInfo)
                        responseResult.onSuccess { deviceInfo ->
                            // 更新服务信息
                            val updatedInfo = ServiceDisplayInfo(
                                serviceDisplayInfo.serviceInfo,
                                deviceInfo,
                                true
                            )
                            val index = serviceInfos.indexOf(serviceDisplayInfo)
                            if (index != -1) {
                                serviceInfos[index] = updatedInfo
                            }
                        }
                        responseResult.onFailure {
                            val updatedInfo = ServiceDisplayInfo(
                                serviceDisplayInfo.serviceInfo,
                                null,
                                false
                            )
                            val index = serviceInfos.indexOf(serviceDisplayInfo)
                            if (index != -1) {
                                serviceInfos[index] = updatedInfo
                            }
                        }
                    }
                }

            }
        }
    }

    private suspend fun isServiceOnline(serviceInfo: ServiceInfo): Result<DeviceInfoResponse> =
        withContext(Dispatchers.IO) {
            try {
                val url = HttpUrl.Builder().scheme("http").host(
                    serviceInfo.inet4Addresses.firstOrNull()?.hostAddress
                        ?: return@withContext Result.failure(IOException("No valid IPv4 address"))
                ).port(serviceInfo.port).build()
                val api = RetrofitClient.getServerAPI(url)
                val response = api.getDeviceInfo().execute()

                if (response.isSuccessful) {
                    val deviceInfo =
                        response.body() ?: throw IOException("Response body is null")
                    return@withContext Result.success(deviceInfo)
                } else {
                    return@withContext Result.failure(IOException("Failed to get device info, HTTP code: ${response.code()}"))

                }
            } catch (e: Exception) {
                return@withContext Result.failure<DeviceInfoResponse>(e)
            }
        }


    // 修改 addUnique 函数以适应新的数据类
    fun MutableList<ServiceDisplayInfo>.addUnique(info: ServiceDisplayInfo) {
        if (!info.serviceInfo.name.contains(JmDnsUtils.SERVICE_NAME_SUFFIX)) {
            return
        }
        if (info.serviceInfo.inet4Addresses.isEmpty()) {
            return
        }
        val exists = any {
            it.serviceInfo.inet4Addresses.firstOrNull() == info.serviceInfo.inet4Addresses.firstOrNull() &&
                    it.serviceInfo.port == info.serviceInfo.port
        }
        if (!exists) {
            add(info)
        }
    }

    suspend fun fetchApplicationExternalStorageFileList(
        ip: String, port: Int, subPath: String
    ): Result<AppDataResponse> {
        return runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val response =
                RetrofitClient.getServerAPI(url).getApplicationExternalStorageList(subPath)
                    .execute()
            return@runCatching response.body() ?: throw IOException("${url}:Response body is null")
        }
    }

    suspend fun fetchInstalledApps(
        ip: String, port: Int
    ): Result<InstalledAppsResponse> {
        return runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val response = RetrofitClient.getServerAPI(url).getInstallApps().execute()
            return@runCatching response.body() ?: throw IOException("${url}:Response body is null")
        }
    }


    suspend fun fetchDeviceInfo(
        ip: String, port: Int
    ): Result<DeviceInfoResponse> {
        return runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val response = RetrofitClient.getServerAPI(url).getDeviceInfo().execute()
            return@runCatching response.body() ?: throw IOException("${url}:Response body is null")
        }
    }


    suspend fun fetchApplicationPrivateStorageFileList(
        ip: String, port: Int, subPath: String
    ): Result<AppDataResponse> {
        return runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val response =
                RetrofitClient.getServerAPI(url).getApplicationPrivateStorageList(subPath).execute()
            return@runCatching response.body() ?: throw IOException("${url}:Response body is null")
        }
    }

    suspend fun fetchExternalStorageFileList(
        ip: String, port: Int, subPath: String
    ): Result<AppDataResponse> {
        return runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val response =
                RetrofitClient.getServerAPI(url).getExternalStorageList(subPath).execute()
            return@runCatching response.body() ?: throw IOException("${url}:Response body is null")
        }
    }


    /**
     * 找出列表元素间的最长公共子串
     * @return 最长公共子串
     */
    fun List<String>.findCommonSubstring(): String {
        if (this.isEmpty()) return ""

        // 以第一个元素作为基准字符串
        val baseString = this[0]
        var longestCommonSubstring = ""

        // 遍历基准字符串的所有可能子串
        for (i in baseString.indices) {
            for (j in i + 1..baseString.length) {
                val substring = baseString.substring(i, j)
                // 检查子串是否存在于列表的其他所有元素中
                if (this.all { it.contains(substring) } && substring.length > longestCommonSubstring.length) {
                    longestCommonSubstring = substring
                }
            }
        }
        return longestCommonSubstring
    }

    suspend fun fetchCurrentServerInoDataList(
        ip: String, port: Int, external: Boolean, isAppStorage: Boolean, subPath: String
    ): Result<AppDataResponse> {
        return if (isAppStorage) {
            if (external) {
                fetchApplicationExternalStorageFileList(
                    ip, port, subPath
                )
            } else {
                fetchApplicationPrivateStorageFileList(
                    ip, port, subPath
                )
            }
        } else {
            fetchExternalStorageFileList(
                ip, port, subPath
            )
        }
    }


    /**
     * 下载文件的方法
     * @param ip 服务器 IP 地址
     * @param port 服务器端口
     * @param filePath 要下载的文件在服务器上的路径
     * @param destFile 下载文件保存的本地目标文件
     * @return 下载结果，成功返回 Success，失败返回 Failure
     */
    suspend fun downloadFile(
        ip: String, port: Int, filePath: String, destFile: File, progress: (Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val api = RetrofitClient.getServerAPI(url)
            val call = api.downloadFile(filePath)
            val response = call.execute()

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    saveFile(body, destFile.outputStream(), progress = progress)
                } ?: throw IOException("Response body is null")
            } else {
                throw IOException("HTTP request failed with code ${response.code()}")
            }
        }
    }

    suspend fun takeScreen(
        ip: String, port: Int, destFile: File, progress: (Int) -> Unit
    ): Result<Unit> {
        return kotlin.runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val api = RetrofitClient.getServerAPI(url)
            val call = api.getScreenshot()
            val response = call.execute()

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    saveFile(body, destFile.outputStream(), progress = progress)
                } ?: throw IOException("Response body is null")
            } else {
                throw IOException("HTTP request failed with code ${response.code()}")
            }
        }
    }

    /**
     * 将响应体保存到本地文件
     * @param body 响应体
     * @param destFile 保存的目标文件
     */
    @Throws
    private suspend fun saveFile(
        body: ResponseBody, outputStream: OutputStream, progress: (Int) -> Unit
    ) {
        try {
            val inputStream = body.byteStream()
            val buffer = ByteArray(4096)
            var bytesRead: Int
            // 获取文件总字节数
            val totalBytes = body.contentLength()
            var downloadedBytes: Long = 0


            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                // 累加已下载的字节数
                downloadedBytes += bytesRead
                if (totalBytes > 0) {
                    // 计算下载进度
                    val progress = (downloadedBytes * 100 / totalBytes).toInt()
                    // 打印下载进度日志

                    progress(progress)
                }
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            throw IOException("Failed to save file", e)
        }
    }

    /**
     * 下载 APK 文件的方法
     * @param ip 服务器 IP 地址
     * @param port 服务器端口
     * @param apkPath 要下载的 APK 文件在服务器上的路径
     * @param progress 下载进度回调
     * @return 下载结果，成功返回 Success，失败返回 Failure
     */
    suspend fun downloadApk(
        ip: String, port: Int, apkPath: String, progress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val api = RetrofitClient.getServerAPI(url)
            val call = api.downloadApk(apkPath)
            val response = call.execute()

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val downloadDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs()
                    }
                    val fileName = apkPath.substringAfterLast("/")
                    val destFile = File(downloadDir, fileName)
                    saveFile(body, destFile.outputStream(), progress = progress)
                    return@runCatching destFile
                } ?: throw IOException("Response body is null")
            } else {
                throw IOException("HTTP request failed with code ${response.code()}")
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        checkOnlineJob?.cancel()
    }


    private val TAG = "MainScanViewModel"

}