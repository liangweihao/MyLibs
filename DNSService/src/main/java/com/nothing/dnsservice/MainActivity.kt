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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.jmdns.ServiceInfo

private val LocalNavController =
    staticCompositionLocalOf<NavHostController> { throw Exception("LocalNavController not provided") }


private val LocalMainScanViewModel =
    staticCompositionLocalOf<MainScanViewModel> { throw Exception("LocalMainScanViewModel not provided") }


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    private val ROUTE_HOME = "home"
    private val ROUTE_FILE_LIST = "file_list"
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
                    })
                }
                this.composable<FileListRouter> {
                    val router = it.toRoute<FileListRouter>()
                    FileListScreen(viewModel, router, { fileRouter ->
                        navController.navigate(fileRouter)
                    }, { rootPath, fileRouter ,loadingState->
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
                                ).path,
                                destFile, {progress ->
                                    loadingState.intValue = progress
                                }
                            ).onSuccess {
                                loadingState.intValue = -1
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        destFile.path,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.onFailure {
                                withContext(Dispatchers.Main){
                                    Toast.makeText(
                                        this@MainActivity,
                                        "文件下载失败,请重试",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                                Lg.w(TAG, "download fail:${Lg.getStackTraceAsString(it)}")
                            }
                        }
                    })
                }
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



