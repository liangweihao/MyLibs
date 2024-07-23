package com.nothing.commonutils.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter


class LocalCrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val TAG = "LocalCrashHandler"
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // 保存崩溃信息到本地
        saveCrashInformation(throwable)
        throwable.printStackTrace()
        Lg.d(
            TAG,
            "uncaughtException() called with: thread = $thread, throwable = ${throwable.message}"
        )
        Process.killProcess(Process.myPid())
    }

    private fun saveCrashInformation(throwable: Throwable) {
        // 构建崩溃信息

        try {
            // 将崩溃信息保存到本地
            val crashInfo = buildCrashInfo(throwable)
            val file = File(
                context.getExternalFilesDir(null),
                "crash_log_${System.currentTimeMillis()}.txt"
            )
            FileWriter(file, true).use { writer ->
                writer.write(crashInfo+"\n")
                try {
                    val activityManager =
                        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val memoryInfo = ActivityManager.MemoryInfo()
                    activityManager.getMemoryInfo(memoryInfo)

                    val availableMemory = memoryInfo.availMem
                    val totalMemory = memoryInfo.totalMem
                    writer.write("availableMemory:$availableMemory,totalMemory:$totalMemory\n")

                }catch (e:Throwable){
                    e.printStackTrace()
                }

                try {
                    val fr = FileReader("/proc/stat")
                    val br = BufferedReader(fr)
                    var line: String
                    while ((br.readLine().also { line = it }) != null) {
                        if (line.startsWith("cpu ")) {
                            val parts = line.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            if (parts.size >= 9) {
                                val user = parts[1].toLong()
                                val nice = parts[2].toLong()
                                val system = parts[3].toLong()
                                val idle = parts[4].toLong()
                                val iowait = parts[5].toLong()
                                val irq = parts[6].toLong()
                                val softirq = parts[7].toLong()
                                val steal = parts[8].toLong()

                                val cpuUsage =
                                    user + nice + system + idle + iowait + irq + softirq + steal
                                Lg.d(TAG, "CPU Usage: $cpuUsage")
                                writer.write("CPU Usage:$cpuUsage\n")
                                break
                            }
                        }
                    }
                    fr.close()
                } catch (e: IOException) {
                    Lg.e(TAG, "Failed to read /proc/stat", e)
                }
                try {
                    val memoryInfo: Debug.MemoryInfo = Debug.MemoryInfo()
                    Debug.getMemoryInfo(memoryInfo)
                    writer.write("Heap Size: " + memoryInfo.totalPss + " KB")
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun buildCrashInfo(throwable: Throwable): String {
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        throwable.printStackTrace(printWriter)
        printWriter.flush()
        return writer.toString()
    }
}
