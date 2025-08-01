package com.nothing.dnsservice.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 格式化文件大小，根据文件大小自动选择合适的单位（B、KB、MB、GB）
 * @param size 文件大小，单位为字节
 * @return 格式化后的文件大小字符串
 */

fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "${size}B"
        size < 1024 * 1024 -> "%.2fKB".format(size / 1024.0)
        size < 1024 * 1024 * 1024 -> "%.2fMB".format(size / (1024.0 * 1024))
        else -> "%.2fGB".format(size / (1024.0 * 1024 * 1024))
    }
}


/**
 * 根据文件的修改时间进行格式化，对相同年、同月、同日的情况进行优化
 * @param modifyTime 文件的修改时间，单位为毫秒
 * @return 格式化后的时间字符串
 */
fun formatFileModifyTime(modifyTime: Long): String {
    val currentCalendar = Calendar.getInstance()
    val fileCalendar = Calendar.getInstance()
    fileCalendar.time = Date(modifyTime)

    return when {
        currentCalendar.get(Calendar.YEAR) != fileCalendar.get(Calendar.YEAR) -> {
            // 不同年份，格式化为 "yyyy-MM-dd HH:mm:ss"
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(modifyTime)
        }

        currentCalendar.get(Calendar.MONTH) != fileCalendar.get(Calendar.MONTH) ||
                currentCalendar.get(Calendar.DAY_OF_MONTH) != fileCalendar.get(Calendar.DAY_OF_MONTH) -> {
            // 同年不同月或同月不同日，格式化为 "MM-dd HH:mm:ss"
            SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(modifyTime)
        }

        else -> {
            // 同年同月同日，格式化为 "HH:mm:ss"
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(modifyTime)
        }
    }
}