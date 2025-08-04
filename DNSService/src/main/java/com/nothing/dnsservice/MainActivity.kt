package com.nothing.dnsservice

//import kotlinx.serialization.Serializable
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nothing.commonutils.utils.Lg
import com.nothing.dnsservice.home.FileListScreen
import com.nothing.dnsservice.home.MainScanViewModel
import com.nothing.dnsservice.home.MainScreen
import com.nothing.dnsservice.home.ScreenShootPreviewScreen
import com.nothing.dnsservice.server.bean.FileInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import javax.jmdns.ServiceInfo
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import com.nothing.commonutils.utils.MimeTypeUtils
import java.io.FileInputStream
import java.io.OutputStream

private val LocalNavController =
    staticCompositionLocalOf<NavHostController> { throw Exception("LocalNavController not provided") }


private val LocalMainScanViewModel =
    staticCompositionLocalOf<MainScanViewModel> { throw Exception("LocalMainScanViewModel not provided") }


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainScanViewModel::class]

        setContent {
            val navController = rememberNavController()
            navController.setLifecycleOwner(this)
            navController.setViewModelStore(this.viewModelStore)
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalMainScanViewModel provides viewModel,
            ) {
                InitContentView()
            }
        }
    }


    @kotlinx.serialization.Serializable
    data class HomeRouter(var name: String)

    @kotlinx.serialization.Serializable
    data class FileListRouter(var subDir: String, var external: Boolean)

    @Serializable
    data class ScreenShoot(var ip: String, var port: Int)

    @Composable
    fun InitContentView() {
        val viewModel = LocalMainScanViewModel.current
        val navController = LocalNavController.current
        var coroutineScope = rememberCoroutineScope()
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = Color.Black)) {
            NavHost(
                navController,
                startDestination = HomeRouter("home"),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                enterTransition = {
                    fadeIn(animationSpec = tween(400))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(400))
                },
            ) {

                this.composable<HomeRouter> {
                    MainScreen(viewModel.inetAddresses, viewModel.serviceInfos, { info ->
                        handlePrivateStorageClick(viewModel, navController, info)
                    }, { info ->
                        handleExternalStorageClick(viewModel, navController, info)
                    }, { info ->
                        handleScreenShootClick(viewModel, navController, info)
                    })
                }
                this.composable<FileListRouter> {
                    val router = it.toRoute<FileListRouter>()
                    FileListScreen(viewModel, router, { fileRouter ->
                        navController.navigate(fileRouter)
                    }, { rootPath, fileRouter, loadingState ->
                        handleDownloadFile(
                            rootPath, fileRouter, loadingState, coroutineScope, viewModel
                        )
                    })
                }
                this.composable<ScreenShoot> {
                    val screenShoot = it.toRoute<ScreenShoot>()
                    ScreenShootPreviewScreen(viewModel, screenShoot) { file ->
                        handleDownloadLocal(viewModel, coroutineScope, file)
                    }
                }
            }
        }

    }

    private fun handleDownloadLocal(
        viewModel: MainScanViewModel, coroutineScope: CoroutineScope, file: File
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val contentResolver: ContentResolver = contentResolver
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    "screenshot_${System.currentTimeMillis()}.jpg"
                )
                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    MimeTypeUtils.getMimeTypeByExtension(MimeTypeUtils.getFileExtension(file))
                )
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageCollection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            try {
                val uri: Uri? = contentResolver.insert(imageCollection, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        FileInputStream(file).use { inputStream ->
                            copyStream(inputStream, outputStream)
                        }
                        // 保存成功
                        showToast("图片保存成功")
                    }
                    contentResolver.query(uri, null, null, null)?.apply {
                        while (moveToNext()) {
                            val dataIndex = getColumnIndex(MediaStore.Images.Media.DATA)
                            val path = getString(dataIndex)
                            Lg.i(TAG, "Save Path ${path}")
                        }
                    }?.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("图片保存失败")
            }
        }

    }

    private fun copyStream(inputStream: FileInputStream, outputStream: OutputStream) {
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleScreenShootClick(
        viewModel: MainScanViewModel, navController: NavHostController, info: ServiceInfo
    ) {
        navController.navigate(ScreenShoot(info.inet4Addresses.first().hostAddress!!, info.port))
    }

    private fun handleDownloadFile(
        rootPath: String,
        fileRouter: FileInfo,
        loadingState: MutableIntState,
        coroutineScope: CoroutineScope,
        viewModel: MainScanViewModel
    ) {
        Lg.i(TAG, "${File(rootPath, fileRouter.path)}.  ${fileRouter}")
        loadingState.intValue = 0
        coroutineScope.launch(Dispatchers.IO) {
            val destFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(fileRouter.path).name
            )
            viewModel.fetchCurrentServerInfoFile(
                File(
                    rootPath, fileRouter.path
                ).path, destFile, { progress ->
                    loadingState.intValue = progress
                }).onSuccess {
                loadingState.intValue = -1
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity, destFile.path, Toast.LENGTH_SHORT
                    ).show()
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity, "文件下载失败,请重试", Toast.LENGTH_SHORT
                    ).show()
                }
                Lg.w(TAG, "download fail:${Lg.getStackTraceAsString(it)}")
            }
        }
    }


    /**
     * 处理服务项点击事件，发起网络请求获取应用数据文件列表。
     *
     * @param viewModel 主扫描 ViewModel 实例
     * @param info 服务信息对象
     * @param connectTimeMs 连接耗时的可变状态
     * @param loading 加载状态的可变状态
     */
    private fun handlePrivateStorageClick(
        viewModel: MainScanViewModel, navController: NavHostController, info: ServiceInfo
    ) {
        // 安全获取 IP 地址

        viewModel.currentServiceInfo = info
        navController.navigate(FileListRouter("", false))

    }

    private fun handleExternalStorageClick(
        viewModel: MainScanViewModel, navController: NavHostController, info: ServiceInfo
    ) {
        viewModel.currentServiceInfo = info
        navController.navigate(FileListRouter("", true))
    }

}



