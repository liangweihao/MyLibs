package com.nothing.dnsservice.home

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.dnsservice.server.RetrofitClient
import com.nothing.dnsservice.server.bean.AppDataResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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


public class MainScanViewModel(application: Application) : AndroidViewModel(application) {

    // 用于存储网络地址信息
    val inetAddresses = mutableStateListOf<String>()
    @Deprecated("")
    var currentServiceInfo: ServiceInfo? = null

    // 用于存储服务解析信息
    val serviceInfos = mutableStateListOf<ServiceInfo>()


    private val dnsCallback = object : JmDnsUtils.JmDnsDiscoveryCallback {
        override fun onServiceAdded(event: ServiceEvent) {}

        override fun onServiceRemoved(event: ServiceEvent) {}

        override fun onServiceResolved(event: ServiceEvent) {

            viewModelScope.launch(Dispatchers.Main) {
                if (event.info.inet4Addresses.isNotEmpty()) {
                    serviceInfos.addUnique(event.info)
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

        }
    }

    fun MutableList<ServiceInfo>.addUnique(info: ServiceInfo) {
        if (!info.name.contains(JmDnsUtils.SERVICE_NAME_SUFFIX)) {
            return
        }
        if (info.inet4Addresses.isEmpty()) {
            return
        }
        val exists = any {
            it.inet4Addresses.firstOrNull() == info.inet4Addresses.firstOrNull() && it.port == info.port

        }
        if (!exists) {
            add(info)
        }
    }

    suspend fun fetchPrivateStorageFileList(
        ip: String,
        port: Int,
        subPath: String
    ): Result<AppDataResponse> {
        return runCatching {
            val url = HttpUrl.Builder().scheme("http").host(ip).port(port).build()
            val response = RetrofitClient.getServerAPI(url).getPrivateStorageList(subPath).execute()
            return@runCatching response.body() ?: throw IOException("${url}:Response body is null")
        }
    }

    suspend fun fetchExternalStorageFileList(
        ip: String,
        port: Int,
        subPath: String
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
        external: Boolean,
        subPath: String
    ): Result<AppDataResponse> {
        return (currentServiceInfo?.let {
            if (external) {
                fetchExternalStorageFileList(
                    it.inet4Addresses.first().hostAddress!!,
                    it.port,
                    subPath
                )
            } else {
                fetchPrivateStorageFileList(
                    it.inet4Addresses.first().hostAddress!!,
                    it.port,
                    subPath
                )
            }
        } ?: Result.failure<AppDataResponse>(Throwable("ServerInfo is Null")))
    }


    suspend fun fetchCurrentServerInfoFile(
        filePath: String,
        destFile: File,
        progress: (Int) -> Unit
    ): Result<Any> {
        return (currentServiceInfo?.let {
            downloadFile(
                it.inet4Addresses.first().hostAddress!!,
                it.port, filePath, destFile = destFile, progress = progress
            )
        } ?: Result.failure(Throwable("ServerInfo is Null")))
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
        ip: String,
        port: Int,
        filePath: String,
        destFile: File,
        progress: (Int) -> Unit
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
        ip: String,
        port: Int,
        destFile: File,
        progress: (Int) -> Unit
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
        body: ResponseBody,
        outputStream: OutputStream,
        progress: (Int) -> Unit
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

    private val TAG = "MainScanViewModel"

}