package com.nothing.dnsservice.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.nothing.commonutils.utils.Lg
import com.nothing.dnsservice.R
import com.nothing.dnsservice.home.router.FileListRouter
import com.nothing.dnsservice.home.vm.MainViewModel
import com.nothing.dnsservice.server.bean.AppDataResponse
import com.nothing.dnsservice.server.bean.FileInfo
import com.nothing.dnsservice.utils.SizeAuto.adapt
import com.nothing.dnsservice.utils.formatFileModifyTime
import com.nothing.dnsservice.utils.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Stack

private const val TAG = "FileListScreen"

@Composable
fun FileListScreen(
    viewModel: MainViewModel,
    initRouter: FileListRouter,
    onBack: () -> Unit,
    onDownloadClick: (String, FileInfo, MutableIntState) -> Unit
) {
    val dataList = remember { mutableStateOf<AppDataResponse?>(null) }
    val fileListHistory = Stack<FileListRouter>().apply { push(initRouter) }
    FileListScreenImpl(
        initRouter,
        viewModel,
        dataList,
        fileListHistory,
        onBack,
        onDownloadClick
    )
}

@Composable
private fun FileListScreenImpl(
    initRouter: FileListRouter,
    viewModel: MainViewModel,
    dataList: androidx.compose.runtime.MutableState<AppDataResponse?>,
    fileListHistory: Stack<FileListRouter>,
    onBack: () -> Unit,
    onDownloadClick: (String, FileInfo, MutableIntState) -> Unit
) {
    var currentRouterState by remember { mutableStateOf<FileListRouter>(initRouter) }
    LaunchedEffect(currentRouterState) {
        Lg.i(TAG, "FileListScreen Launch Effect  ")
        withContext(Dispatchers.IO) {
            viewModel.fetchCurrentServerInoDataList(
                currentRouterState.ip,
                currentRouterState.port,
                currentRouterState.external,
                currentRouterState.isAppStorage,
                currentRouterState.currentSubDir
            ).onSuccess { response ->
                val rootPath = response.rootPath
                response.data = response.data.map { fileInfo ->
                    val formattedTime = formatFileModifyTime(fileInfo.modifyTime)
                    val formattedSize = formatFileSize(fileInfo.length)
                    fileInfo.formattedDes =
                        if (fileInfo.mimeType.isEmpty()) formattedTime else "$formattedTime    $formattedSize"
                    fileInfo.displayName = File(fileInfo.path).name
                    fileInfo.path = fileInfo.path.replace(rootPath, "")
                    fileInfo
                }.sortedWith(compareBy({ it.mimeType.isNotEmpty() }, { it.path.lowercase() }))
                dataList.value = response
            }.onFailure {
                dataList.value = null
            }
        }
    }

    BackHandler {
        if (fileListHistory.isEmpty()) {
            onBack()
        } else {
            fileListHistory.pop()
            if (fileListHistory.isNotEmpty()) {
                currentRouterState = fileListHistory.peek()
            } else {
                onBack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
    ) {
        Row {
            val subDirText = if (currentRouterState.currentSubDir.isEmpty()) "" else ":"
            Text(
                "远程文件列表$subDirText",
                fontSize = TextUnit(32.dp.adapt().value, TextUnitType.Sp),
                modifier = Modifier.padding(
                    start = 36.dp.adapt(), top = 8.dp.adapt(), bottom = 8.dp.adapt()
                ),
                color = Color.Black
            )
            val listState = rememberLazyListState()
            LazyRow(modifier = Modifier.weight(1f), state = listState) {
                item {
                    // 修复 Redundant string template 问题
                    Text(
                        currentRouterState.currentSubDir,
                        fontSize = TextUnit(32.dp.adapt().value, TextUnitType.Sp),
                        modifier = Modifier.padding(
                            start = 0.dp.adapt(),
                            top = 8.dp.adapt(),
                            end = 36.dp.adapt(),
                            bottom = 8.dp.adapt()
                        ),
                        color = Color.Black
                    )
                }
            }
        }

        dataList.value?.let { response ->
            val list = response.data
            if (list.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = list, key = { item -> item.path }) { item ->
                        val loadingState = remember { mutableIntStateOf(-1) }
                        val rowModifier = Modifier
                            .padding(horizontal = 30.dp.adapt(), vertical = 5.dp.adapt())
                            .background(Color.White, shape = RoundedCornerShape(20.dp.adapt()))
                            .clickable {
                                if (item.mimeType.isNotEmpty()) {
                                    onDownloadClick.invoke(response.rootPath, item, loadingState)
                                } else {
                                    val listRouter = FileListRouter(
                                        currentRouterState.ip,
                                        currentRouterState.port,
                                        File(currentRouterState.currentSubDir, item.path).path,
                                        currentRouterState.external,
                                        currentRouterState.isAppStorage,
                                        false
                                    )
                                    fileListHistory.push(listRouter)
                                    currentRouterState = listRouter
                                }
                            }
                            .padding(30.dp.adapt(), 36.dp.adapt())

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = rowModifier
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(if (item.mimeType.isEmpty()) R.drawable.icon_mime_dir else R.drawable.icon_mime_file),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp.adapt())
                                )

                                if (loadingState.intValue > -1) {
                                    CircularProgressIndicator(
                                        progress = { loadingState.intValue / 100f },
                                        modifier = Modifier.size(40.dp.adapt())
                                    )
                                }
                            }

                            Column(modifier = Modifier.wrapContentHeight()) {
                                Text(
                                    item.displayName ?: "",
                                    modifier = Modifier
                                        .padding(
                                            start = 30.dp.adapt(), end = 0.dp.adapt()
                                        )
                                        .fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = TextUnit(24.dp.adapt().value, TextUnitType.Sp),
                                )
                                Text(
                                    item.formattedDes ?: "",
                                    modifier = Modifier
                                        .padding(
                                            start = 30.dp.adapt(), end = 0.dp.adapt(), bottom = 0.dp
                                        )
                                        .fillMaxWidth(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = TextUnit(24.dp.adapt().value, TextUnitType.Sp),
                                )
                            }
                        }
                    }
                }
            } else {
                EmptyState()
            }
        } ?: LoadingState()
    }
}

@Composable
private fun LoadingState() {
    Lg.i(TAG, "正在加载中")
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp.adapt()))
            Text("正在加载中", modifier = Modifier.wrapContentSize())
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val vectorPainter = rememberVectorPainter(Icons.Sharp.Warning)
        Image(
            painter = vectorPainter,
            contentDescription = "Warning Icon",
            colorFilter = ColorFilter.tint(
                Color.Red, blendMode = BlendMode.SrcIn
            )
        )
        Text(
            "文件空空如也",
            modifier = Modifier.wrapContentSize(),
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.Gray
        )
    }
}
