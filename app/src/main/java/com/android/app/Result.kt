package com.android.app

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioRecord.MetricsConstants.CHANNELS
import android.util.Log
import com.android.app.ObjectDetector.Box
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList


data class Result(
    var output: ArrayList<Box>
) {

}

var INPUT_IMG_WIDTH = 640L
var INPUT_IMG_HEIGHT = 640L

class ObjectDetector(
) {

    fun flattenImageData(
        imageData: FloatArray,
        width: Int,
        height: Int,
        channels: Int
    ): FloatArray {
        val flattenedData = FloatArray(width * height * channels)
        var index = 0
        for (c in 0 until channels) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    flattenedData[index++] = imageData[c * width * height + y * width + x]
                }
            }
        }
        return flattenedData
    }


    data class Box(
        var centerX: Float, // box的中心点X
        var centerY: Float,  // box的中心点Y
        var width: Float,  // box的width
        var height: Float,  // box的height
        var conf: Float,
        var cat:Int
    )

    private val TAG = "Result"

    fun detect(bitmap: Bitmap, ortEnv: OrtEnvironment, ortSession: OrtSession): Result {
        // Step 1: convert image into byte array (raw image bytes)
//        val rawImageBytes = inputStream.readBytes()

        // Step 2: get the shape of the byte array and make ort tensor
        val shape = longArrayOf(1, 3, INPUT_IMG_WIDTH, INPUT_IMG_HEIGHT)
//        val shape = longArrayOf(inputStream.remaining().toLong())
//        val shape = longArrayOf(rawImageBytes.size.toLong())
        var time = System.currentTimeMillis()
        val inputBuffer = FloatBuffer.allocate((1 * 3 * INPUT_IMG_WIDTH * INPUT_IMG_HEIGHT).toInt())
        for (c in 0 until 3) {
            for (y in 0 until INPUT_IMG_HEIGHT) {
                for (x in 0 until INPUT_IMG_WIDTH) {
                    val pixel: Int = bitmap.getPixel(x.toInt(), y.toInt())
                    var value = if (c == 0) {
                        ((pixel shr 16) and 0xFF) / 255.0f // R
                    } else if (c == 1) {
                        ((pixel shr 8) and 0xFF) / 255.0f // G
                    } else {
                        (pixel and 0xFF) / 255.0f // B
                    }
                    inputBuffer.put(value)
                }
            }
        }
        Log.d(TAG,"Get Pixel ${System.currentTimeMillis() - time}ms")
        time = System.currentTimeMillis()
        inputBuffer.rewind()
        val inputTensor = OnnxTensor.createTensor(
            ortEnv,
            inputBuffer,
            shape
        )
        inputTensor.use {
            // Step 3: call ort inferenceSession run
            val output = ortSession.run(
                Collections.singletonMap("images", inputTensor),
                setOf("output0")
            )
            Log.d(TAG,"Inference ${System.currentTimeMillis() - time}ms")
            time = System.currentTimeMillis()
            // Step 4: output analysis
            output.use {
                var boxes = ArrayList<Box>()

                output?.get(0)?.value?.let { it as? Array<Array<FloatArray>> }?.also { tensor ->

                    var batch_size = tensor.size
                    var box_array: Array<FloatArray> = tensor[0]
                    var infoSize = box_array.size // 84
                    var predict_sample_count = box_array[0].size // 8400


                    var positionSize = 4
                    for (i in 0 until predict_sample_count) {
                        var centerX: Float = 0f
                        var centerY: Float = 0f
                        var width: Float = 0f
                        var height: Float = 0f
                        var confArray: FloatArray = FloatArray(infoSize - positionSize)
                        for (j in 0 until infoSize) {
                            if (j == 0) {
                                centerX = box_array[j][i]
                            } else if (j == 1) {
                                centerY = box_array[j][i]
                            } else if (j == 2) {
                                width = box_array[j][i]
                            } else if (j == 3) {
                                height = box_array[j][i]
                            }else{
                                confArray[j - positionSize] = box_array[j][i]
                            }
                        }
                        for (index in 0 until confArray.size) {
                            if(confArray[index] > 0.25){
                                var box = Box(centerX,centerY,width,height,confArray[index],index)
                                boxes.add(box)
                                break
                            }
                        }
                    }

                }
                Log.d(TAG,"Boxes ${System.currentTimeMillis() - time}ms")

                var result = Result(boxes)
                return result
            }
        }
    }

    private fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}