package com.nothing.commonutils.utils

import android.content.Context
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Logcat日志保存到本地工具类
 * 特性：子线程执行、缓冲流写入、私有目录存储（无需权限）、自动命名、资源自动释放
 */
object LogcatToFileUtil {
    // 日志文件存储目录（应用私有目录：/data/data/包名/files/logcat/）
    private const val LOG_DIR_NAME = "logcat"

    // 单个日志文件最大50MB，避免占满存储
    private const val MAX_LOG_FILE_SIZE = (1024 * 1024 * 50).toLong()

    // 单线程池：保证日志写入顺序，避免多线程文件冲突
    private val LOG_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()

    // 日志写入流（全局单例，减少创建开销）
    private var sLogWriter: BufferedWriter? = null

    // 当前日志文件
    private var sCurrentLogFile: File? = null

    // 采集状态标记
    private var isCapturing = false

    /**
     * 初始化日志文件（应用启动时调用，建议在Application中初始化）
     * @param context 上下文，推荐使用Application Context，避免内存泄漏
     */
    fun initLogFile(context: Context) {
        if (sCurrentLogFile != null && sLogWriter != null) {
            return
        }
        // 1. 获取应用私有文件目录（Android 10+ 无需存储权限，安全且适配）
        val logDir = File(context.externalCacheDir, LOG_DIR_NAME)
        if (!logDir.exists()) {
            val isCreated = logDir.mkdirs()
            if (!isCreated) {
                return
            }
        }
        // 2. 日志文件命名：logcat_20260202_153000.txt（时间戳，方便区分和后续查找）
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val fileName = "logcat_" + sdf.format(Date()) + ".txt"
        sCurrentLogFile = File(logDir, fileName)

        // 3. 初始化缓冲写入流（追加模式：FileWriter(true)）
        try {
            sLogWriter = BufferedWriter(FileWriter(sCurrentLogFile, true), 2048)
        } catch (e: IOException) {
            e.printStackTrace()
            releaseResources()
        }
    }

    /**
     * 启动Logcat采集并写入本地文件
     * 可在Application onCreate、Activity onCreate中调用，实现应用全程日志采集
     */
    fun startLogcatCapture(context: Context) {
        if (isCapturing || sLogWriter == null || sCurrentLogFile == null) {
            return
        }
        isCapturing = true

        // 核心：在单线程池中执行，避免主线程阻塞
        LOG_EXECUTOR.execute {
            var logcatProcess: Process? = null
            var inputStream: InputStream? = null
            var br: BufferedReader? = null
            try {
                // 构建logcat命令：捕获全量日志，带时间戳/进程ID/线程ID，方便分析
                // 命令说明：-v time 日志格式（时间+PID+TID+标签+内容）；*:V 捕获所有级别日志（Verbose-Debug-Info-Warn-Error-Fatal）
                val logcatCmd = arrayOf("logcat", "-v", "time", "*:V")
                // 执行logcat命令，获取日志输入流
                logcatProcess = Runtime.getRuntime().exec(logcatCmd)
                inputStream = logcatProcess.inputStream
                br = BufferedReader(InputStreamReader(inputStream), 1024)

                var logLine: String = ""
                // 实时读取日志行，同步写入文件
                while (isCapturing && (br.readLine().also { logLine = it }) != null) {
                    writeLogLine(context, logLine)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                // 释放流和进程资源
                try {
                    br?.close()
                    inputStream?.close()
                    logcatProcess?.destroy()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // 停止采集后释放核心资源
                releaseResources()
            }
        }
    }

    /**
     * 写入单条日志到文件（内部方法，自动处理文件大小和流刷新）
     */
    private fun writeLogLine(context: Context, logLine: String) {
        if (sLogWriter == null || sCurrentLogFile == null) {
            return
        }
        try {
            // 检查文件大小，超过最大值自动创建新文件，避免单个文件过大
            if (sCurrentLogFile!!.length() >= MAX_LOG_FILE_SIZE) {

                sLogWriter!!.write("------------------------- Log is Max Size -------------------------")
                sLogWriter!!.newLine() // 换行
                releaseResources() // 关闭旧流
                // 重新初始化新文件（需传入Application Context，此处替换为你的Application）
                initLogFile(context)
                sLogWriter!!.write("------------------------- Log is Max Size And Continue -------------------------")
                sLogWriter!!.newLine() // 换行
            }
            // 写入日志：可额外添加自定义时间戳（可选，logcat本身已有时间）
            val customTime =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(Date())
            sLogWriter!!.write("$customTime | $logLine")
            sLogWriter!!.newLine() // 换行
            sLogWriter!!.flush() // 强制刷新缓冲，避免进程意外终止导致日志丢失
        } catch (e: IOException) {
            e.printStackTrace()
            releaseResources()
        }
    }

    /**
     * 停止Logcat采集
     * 建议在Application onTerminate、Activity onDestroy中调用
     */
    fun stopLogcatCapture() {
        isCapturing = false
        releaseResources()
    }

    /**
     * 释放日志写入流和文件资源
     */
    private fun releaseResources() {
        try {
            if (sLogWriter != null) {
                sLogWriter!!.close()
                sLogWriter = null
            }
            sCurrentLogFile = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 获取日志文件存储的绝对路径（方便后续导出/查看）
     * @param context 上下文
     * @return 日志文件夹路径
     */
    fun getLogDirPath(): String {
        return sCurrentLogFile?.parent?:""
    }

    /**
     * 过滤采集：仅捕获当前应用的日志（避免全量日志过大，开发调试常用）
     * 替换startLogcatCapture中的logcatCmd即可，参数为当前应用包名
     */
    fun getAppOnlyLogcatCmd(packageName: String): Array<String> {
        // 命令说明：包名:V 仅捕获该应用所有级别日志；*:S 屏蔽其他所有应用日志
        return arrayOf("logcat", "-v", "time", "$packageName:V", "*:S")
    }
}