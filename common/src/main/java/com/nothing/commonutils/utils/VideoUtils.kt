package com.nothing.commonutils.utils

import android.media.MediaMetadataRetriever
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit


object VideoUtils {

    fun getVideoDuration(
        videoPath: String,
        long: Long = 180,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): Long {
        try {
            val longTask = FutureTask<Long>(Callable {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoPath)
                val durationStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLong() ?: 0
                retriever.release()
                return@Callable duration
            })
            Thread(longTask).start()
            return longTask.get(long, timeUnit)
        } catch (e: Throwable) {
//            e.printStackTrace()
        }
        return 0
    }
}