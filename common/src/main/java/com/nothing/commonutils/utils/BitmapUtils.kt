package com.nothing.commonutils.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit


object BitmapUtils {

    private val TAG = "GlideUtils"


    //    const val VIDEO_FRAME  = 20_000_000L mediaprovider 获取的是 时长的一半 ThumbnailUtils.createVideoThumbnail(queryForDataFile(uri, signal),
    @Deprecated("获取视频的最大关键帧https://blog.csdn.net/xiaxl/article/details/67637030/?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0--blog-125645981.235^v38^pc_relevant_default_base&spm=1001.2101.3001.4242.1&utm_relevant_index=1")
    const val VIDEO_FRAME = 0L

    fun getVideoThumbFrame(
        path: String,
        long: Long = 180,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): Long {
        return VideoUtils.getVideoDuration(path, long, timeUnit) * 1000 / 2
    }

    fun getBitmapSize(
        contentResolver: ContentResolver,
        file: Uri,
        options: BitmapFactory.Options = BitmapFactory.Options()
    ): Size {
        try {
            options.inJustDecodeBounds = true
            val openInputStream = contentResolver.openInputStream(file)
            BitmapFactory.decodeStream(openInputStream, null, options)
            return Size(options.outWidth, options.outHeight).also {
                kotlin.runCatching {
                    openInputStream?.close()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Size(0, 0)
    }

    fun getFileBitmap(context: Context, uri: Uri): Bitmap? {
        return getSafeBitmap(context, uri, Int.MAX_VALUE)
    }

    fun viewToBitmap(view: View): Bitmap {
        val width = view.measuredWidth
        val height = view.measuredHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun bitmapToBitmap(inputBitmap: Bitmap, outSize: Size): Bitmap {
        val createBitmap = Bitmap.createBitmap(outSize.width, outSize.height, inputBitmap.config)

        val canvas = Canvas(createBitmap)

        canvas.drawBitmap(inputBitmap, 0f, 0f, null)

        return createBitmap
    }

    fun isTooLarge(bitmap: Bitmap): Boolean {
        return bitmap.byteCount > 99 * 1024 * 1024
    }

    // 将两个 Bitmap 合并为一个双目图像
    fun mergeBitmaps(leftBitmap: Bitmap?, rightBitmap: Bitmap?): Bitmap? {
        // 确保两个 Bitmap 非空
        if (leftBitmap == null || rightBitmap == null) {
            return null
        }

        // 获取两个 Bitmap 的宽度和高度
        val width = Math.max(leftBitmap.width, rightBitmap.width)
        val height = Math.max(leftBitmap.height, rightBitmap.height)

        // 创建一个新的 Bitmap，宽度为两个 Bitmap 宽度之和，高度为最大高度
        val mergedBitmap = Bitmap.createBitmap(width * 2, height, leftBitmap.config)

        // 创建一个画布，将两个 Bitmap 绘制到新的 Bitmap 上
        val canvas = Canvas(mergedBitmap)

        // 绘制左眼视图
        canvas.drawBitmap(leftBitmap, 0f, 0f, null)

        // 绘制右眼视图
        canvas.drawBitmap(rightBitmap, width.toFloat(), 0f, null)
        return mergedBitmap
    }




    // 分割左右眼
    fun split3DBitmaps(input3D:Bitmap?,recycleInput: Boolean = false): Pair<Bitmap,Bitmap>? {
        // 确保两个 Bitmap 非空
        if (input3D == null) {
            return null
        }
        val width: Int = input3D.width
        val height: Int = input3D.height
        val dividedWidth = width / 2
        val leftBitmap = Bitmap.createBitmap(input3D, 0, 0, dividedWidth, height)
        val rightBitmap = Bitmap.createBitmap(input3D, dividedWidth, 0, dividedWidth, height)
        if (recycleInput) {
            input3D.recycle()
        }
        return Pair(leftBitmap,rightBitmap)
    }



    /**
     * @return null 代表 分配失败或者是不需要分配
     * */
    fun alignBitmap(bitmap: Bitmap?, recycleInputBitmap: Boolean = true): Bitmap? {
        if (bitmap == null) {
            return null;
        }
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = roundToEven(width)
        val newHeight = roundToEven(height)
        if (width == newWidth && height == newHeight) {
            return null
        }
        // 创建一个新的位图，并将原始位图绘制到其中
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (recycleInputBitmap) {
            bitmap.recycle() // 释放原始位图的内存
        }
        return resizedBitmap
    }

    // 格式化成偶数
    fun roundToEven(value: Int): Int {
        return if (value % 2 == 0) value else value + 1
    }

    fun drawCenteredBitmap(canvas: Canvas, bitmap: Bitmap) {
        // 获取 Canvas 的宽度和高度
        // 获取 Canvas 的宽度和高度
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height

        // 获取 Bitmap 的宽度和高度

        // 获取 Bitmap 的宽度和高度
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // 计算 Bitmap 在 Canvas 上的绘制位置和缩放比例，使其居中填充

        // 计算 Bitmap 在 Canvas 上的绘制位置和缩放比例，使其居中填充
        val scaleX = canvasWidth.toFloat() / bitmapWidth
        val scaleY = canvasHeight.toFloat() / bitmapHeight
        val scale = Math.min(scaleX, scaleY) // 选择最小的缩放比例，以保持位图的长宽比

        val scaledBitmapWidth = (bitmapWidth * scale).toInt()
        val scaledBitmapHeight = (bitmapHeight * scale).toInt()
        val left = (canvasWidth - scaledBitmapWidth) / 2
        val top = (canvasHeight - scaledBitmapHeight) / 2
        val right = left + scaledBitmapWidth
        val bottom = top + scaledBitmapHeight

        // 绘制 Bitmap

        // 绘制 Bitmap
        val srcRect = Rect(0, 0, bitmapWidth, bitmapHeight) // 原始位图的区域

        val destRect = Rect(left, top, right, bottom) // 目标位图在 Canvas 上的区域

        canvas.drawBitmap(bitmap, srcRect, destRect, null)
    }

    fun getSafeBitmap(context: Context, uri: Uri, maxHeight: Int = Int.MAX_VALUE): Bitmap? {
        var width: Int = 0
        var height: Int = 0
        var ow: Int = 0
        var oh: Int = 0
        var startTime = System.currentTimeMillis()
        val options = BitmapFactory.Options()
        var scale: Int = 1
        var newTime = 0L
        var bitmapSizeString = ""
        var resultHeight: Int = 0
        var resultWidth: Int = 0
        try {
            val bitmapSize = getBitmapSize(context.contentResolver, uri, options)
            newTime = System.currentTimeMillis() - startTime
            width = bitmapSize!!.width
            height = bitmapSize!!.height

            ow = width
            oh = height

            var wh = width / height
            if (maxHeight != Int.MAX_VALUE) {
                height = Math.min(maxHeight, height)
            }
            width = height * wh
            // 偶数可以保证字节宽度和高度是偶数级别的
            var maxBitmapSize = 98 * 1024 * 1024
            while (width * height * 4 > maxBitmapSize) {
                width = (width * 0.98f).toInt()
                height = (height * 0.98f).toInt()

            }
            // 无论小数点的状态 都向上取值 避免上面对于large的计算失效
            scale = Math.max(1, (oh / height))
            if ((ow * oh * 4) / scale > maxBitmapSize) {
                scale = Math.min(1, (oh / height))
            }
            // 设置为非2的幂时，可能会导致图像质量下降或者性能问题的情况主要有以下几种：
            //
            //图像失真： 当 inSampleSize 设置为非2的幂时，系统需要对图像进行不均匀的缩放，这可能会导致图像失真，特别是在边缘部分。
            //
            //内存占用增加： 如果 inSampleSize 设置为非2的幂，系统可能需要更多的内存来存储缩小后的图像，因为它们的尺寸可能无法完全利用系统内存的对齐优势。
            //
            //性能下降： 缩放因子不是2的幂可能会导致系统执行更多的计算来调整图像的大小，这可能会增加处理时间，从而导致性能下降，特别是在大型图像上。
            scale = if (scale == 1) 1 else roundToEven(scale)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inSampleSize = scale
//            options.inSampleSize = Math.ceil(Math.max(1f, (oh / height)).toDouble()).toInt()
            val inputStream = context.contentResolver.openInputStream(uri)

            // TODO:LWH  2024/6/12 如果 bitmap 给 opengl 使用，就得保证宽度和高度是 2 的倍数 否则就会导致  Fatal signal 11 (SIGSEGV), code 2 (SEGV_ACCERR), fault addr 0x75dc8e7000
            var decodeBitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null

            // 格式化  最大的高度
            if (maxHeight != Int.MAX_VALUE) {
                if (decodeBitmap.height > maxHeight) {
                    var originBitmap = decodeBitmap
                    decodeBitmap = Bitmap.createScaledBitmap(
                        decodeBitmap,
                        ((decodeBitmap.width.toFloat() * maxHeight) / decodeBitmap.height.toFloat()).toInt(),
                        maxHeight, false
                    )
                    originBitmap.recycle()
                }
            }

            val currentTimeMillis = System.currentTimeMillis()
            var alignBitmap = alignBitmap(decodeBitmap)
            if (alignBitmap != null) {
                decodeBitmap = alignBitmap
                Lg.d(
                    TAG,
                    "align bitmap time cost:${System.currentTimeMillis() - currentTimeMillis}ms"
                )
            }


            resultWidth = decodeBitmap.width
            resultHeight = decodeBitmap.height

            return decodeBitmap.also {
                if (isTooLarge(it)) {
                    Lg.e(
                        TAG,
                        "获取bitmap太大了,${FileUtils.formatFileSize(it.byteCount.toLong())},${uri}"
                    )
                }
                try {
                    inputStream?.close()
                    bitmapSizeString = FileUtils.formatFileSize(it?.byteCount?.toLong() ?: 0)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

//            return Glide.with(context).asBitmap().load(file)
//                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
//                .submit(width.toInt(), height.toInt()).get()

            //            var mDecoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                BitmapRegionDecoder.newInstance(inputStream!!)
//            } else {
//                BitmapRegionDecoder.newInstance(inputStream!!,false)
//            }
//            val bitmapRect = Rect(0, 0, ow.toInt(), oh.toInt())
//            var bitmap = mDecoder!!.decodeRegion(
//                bitmapRect,
//                options
//            ).also {
//                try {
//                    inputStream.close()
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                }
//
//            }
//            return bitmap
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            Log.d(
                TAG,
                "getSafeBitmap${uri.path}\n:maxHeight:${maxHeight} (${ow}:${oh})-${scale}-Exe:(${width},${height})-Result:(${resultWidth},${resultHeight})" +
                        " buildTime:${System.currentTimeMillis() - startTime}ms ${bitmapSizeString}}"
            )
        }
        return null
    }

    // 压缩改为100
    fun encode(
        bitmap: Bitmap, file: File, quality: Int = 100
    ): Boolean {
        var startTime = System.currentTimeMillis()
        val format: CompressFormat = getFormat(bitmap)
        try {
            var success = false
            var os: OutputStream? = null
            try {
                os = FileOutputStream(file)
                bitmap.compress(format, quality, os)
                os.close()
                success = true
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (os != null) {
                    try {
                        os.close()
                    } catch (e: IOException) {
                        // Do nothing.
                    }
                }
            }
            return success
        } finally {

            Log.d(
                TAG,
                "保存文件到本地:${file.path} ${System.currentTimeMillis() - startTime}ms (${bitmap.width}:${bitmap.height})大小：${
                    FileUtils.formatFileSize(file.length())
                }"
            )
        }
    }

    private fun getFormat(bitmap: Bitmap): CompressFormat {
        return if (bitmap.hasAlpha()) {
            CompressFormat.PNG
        } else {
            CompressFormat.JPEG
        }
    }

    fun bitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
    }
}