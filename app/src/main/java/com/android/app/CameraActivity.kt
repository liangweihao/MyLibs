package com.android.app

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.extensions.OrtxPackage
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.android.app.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.math.min


/**
 *--------------------
 *<p>Author：
 *         lwh
 *<p>Created Time:
 *          2025/3/21
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
class CameraActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding
    private val TAG = "CameraActivity"
    lateinit var ortSession: OrtSession
    var objectDetector = ObjectDetector()

//    lateinit var glRender: CameraGLRenderer
    var cameraExecutor = Executors.newSingleThreadExecutor();
    private var ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        OpenCVLoader.initLocal()
        setContentView(mainBinding.root)
//        val sessionOptions: OrtSession.SessionOptions = OrtSession.SessionOptions()
//        sessionOptions.registerCustomOpLibrary(OrtxPackage.getLibraryPath())
//        ortSession = ortEnv.createSession(readModel(), sessionOptions)

//
//        glRender = object : CameraGLRenderer(mainBinding.cameraView) {
//            override fun openCamera(id: Int) {
//                Log.d(TAG, "openCamera() called with: id = $id")
//            }
//
//            override fun setCameraPreviewSize(width: Int, height: Int) {
//                Log.d(TAG, "setCameraPreviewSize() called with: width = $width, height = $height")
////                initCamera(width, height)
//            }
//
//            override fun closeCamera() {
//                Log.d(TAG, "closeCamera() called")
//            }
//        }
//        glRender.setTexListener(object : CameraGLRenderer.CameraTextureListener {
//            override fun onCameraViewStarted(width: Int, height: Int) {
//                Log.d(TAG, "onCameraViewStarted() called with: width = $width, height = $height")
//
//            }
//
//            override fun onCameraViewStopped() {
//                Log.d(TAG, "onCameraViewStopped() called")
//            }
//
//            override fun onCameraTexture(
//                texIn: Int, texOut: Int, width: Int, height: Int
//            ): Boolean {
////                Log.d(
////                    TAG,
////                    "onCameraTexture() called with: texIn = $texIn, texOut = $texOut, width = $width, height = $height"
////                )
//                return false
//            }
//
//        })

//        Thread{
//            var toBitmap = BitmapFactory.decodeStream(readTestInputImage())
//            detect(toBitmap)
//        }.start()

        Thread{

            val broker = "wss://ms.inair.cn:8084"
            val clientId = "demo_client"

            val client: MqttClient = MqttClient(broker, clientId, MqttDefaultFilePersistence(filesDir.path))
            val options: MqttConnectOptions = MqttConnectOptions()
            options.userName = "inair"
            options.password = "duoping@123".toCharArray()
            client.setCallback(object :MqttCallback{
                override fun connectionLost(cause: Throwable?) {
                    Log.d(TAG, "connectionLost() called with: cause = $cause")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(TAG, "messageArrived() called with: topic = $topic, message = $message")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "deliveryComplete() called with: token = $token")
                }

            })
            client.connect(options)

            Log.i(TAG,"MQTT Client Result " + client.isConnected)
            val message = MqttMessage()
            message.payload = "testMessage ".toByteArray()
            message.qos = 1
            message.isRetained = true
            client.publish("test1",message)
        }.start()





    }

    private fun readModel(): ByteArray {
        val modelID = R.raw.yolo12n_detect
        return resources.openRawResource(modelID).readBytes()
    }
    // 类别映射
    val categoryMap = mapOf(
        0 to "person", 1 to "bicycle", 2 to "car", 3 to "motorcycle", 4 to "airplane",
        5 to "bus", 6 to "train", 7 to "truck", 8 to "boat", 9 to "traffic light",
        10 to "fire hydrant", 11 to "stop sign", 12 to "parking meter", 13 to "bench",
        14 to "bird", 15 to "cat", 16 to "dog", 17 to "horse", 18 to "sheep",
        19 to "cow", 20 to "elephant", 21 to "bear", 22 to "zebra", 23 to "giraffe",
        24 to "backpack", 25 to "umbrella", 26 to "handbag", 27 to "tie",
        28 to "suitcase", 29 to "frisbee", 30 to "skis", 31 to "snowboard",
        32 to "sports ball", 33 to "kite", 34 to "baseball bat", 35 to "baseball glove",
        36 to "skateboard", 37 to "surfboard", 38 to "tennis racket", 39 to "bottle",
        40 to "wine glass", 41 to "cup", 42 to "fork", 43 to "knife", 44 to "spoon",
        45 to "bowl", 46 to "banana", 47 to "apple", 48 to "sandwich", 49 to "orange",
        50 to "broccoli", 51 to "carrot", 52 to "hot dog", 53 to "pizza", 54 to "donut",
        55 to "cake", 56 to "chair", 57 to "couch", 58 to "potted plant", 59 to "bed",
        60 to "dining table", 61 to "toilet", 62 to "tv", 63 to "laptop", 64 to "mouse",
        65 to "remote", 66 to "keyboard", 67 to "cell phone", 68 to "microwave",
        69 to "oven", 70 to "toaster", 71 to "sink", 72 to "refrigerator", 73 to "book",
        74 to "clock", 75 to "vase", 76 to "scissors", 77 to "teddy bear",
        78 to "hair drier", 79 to "toothbrush"
    )
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun detect(inputBitmap: Bitmap){

        var time = System.currentTimeMillis()
        var toBitmap = convertARGBToRGB565(inputBitmap)
        toBitmap = resizeBitmap(toBitmap, INPUT_IMG_WIDTH.toInt(), INPUT_IMG_HEIGHT.toInt())
        Log.d(TAG,"Convert Bitmap ${System.currentTimeMillis() - time}ms")
        time = System.currentTimeMillis()
        try {
            val detect = objectDetector.detect(
                toBitmap,
                ortEnv,
                ortSession
            )
            Log.d(TAG,"Detect Bitmap ${System.currentTimeMillis() - time}ms")
            time = System.currentTimeMillis()

            val paint = Paint()
            paint.color = Color.RED
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            var canvas = Canvas(toBitmap)
            // 初始化文本画笔
            val textPaint = Paint()
            textPaint.color = Color.BLUE
            textPaint.textSize = 28f
            textPaint.textAlign = Paint.Align.CENTER
            detect.output.forEach {  box ->
                val left = box.centerX - box.width / 2
                val top = box.centerY - box.height / 2
                val right = box.centerX + box.width / 2
                val bottom = box.centerY + box.height / 2
                canvas.drawRect(left, top, right, bottom, paint)
                // 计算文本绘制的位置（边框顶部中心）
                val textX = box.centerX
                val textY = top - textPaint.fontMetrics.bottom
                val formattedConf = "%.2f".format(box.conf)
                // 获取类别名称
                val categoryName = categoryMap[box.cat] ?: ""
                // 构建要绘制的文本
                val text = " ${categoryName},${formattedConf}"
                // 绘制文本
                canvas.drawText(text, textX, textY, textPaint)

            }
            Log.d(TAG,"Draw Bitmap ${System.currentTimeMillis() - time}ms")
            mainBinding.ivImg.post {
                mainBinding.ivImg.setImageBitmap(toBitmap)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }



    }

    private fun initCamera(width: Int, height: Int) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@CameraActivity)
        cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val resolutionSelector = ResolutionSelector.Builder().setResolutionStrategy(
                ResolutionStrategy(
                    Size(width, height), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
            ).build()
            val imageAnalysis = ImageAnalysis.Builder().setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            imageAnalysis.setAnalyzer(
                cameraExecutor
            ) { image ->
//                Log.d(
//                    TAG,
//                    "onCreate() called with: width = ${image.width} height = ${image.height}"
//                )
                val rotationDegrees = image.imageInfo.rotationDegrees
                detect(image.toBitmap())

                image.close()
            }
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this@CameraActivity, cameraSelector, imageAnalysis
                )
                println(camera)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this@CameraActivity))
    }

    fun convertMatToByteBuffer(mat: Mat): ByteBuffer {
        // 计算 Mat 对象所需的字节数
        val bufferSize = mat.channels() * mat.cols() * mat.rows()
        // 创建一个直接的 ByteBuffer，大小为计算得到的字节数
        val byteBuffer = ByteBuffer.allocateDirect(bufferSize)
        // 创建一个与 ByteBuffer 大小相同的字节数组
        val byteArray = ByteArray(bufferSize)
        // 将 Mat 对象的数据复制到字节数组中
        mat[0, 0, byteArray]
        // 将字节数组中的数据写入 ByteBuffer
        byteBuffer.put(byteArray)
        // 将 ByteBuffer 的位置重置为 0，以便后续读取
        byteBuffer.flip()
        return byteBuffer
    }

    fun convertMatToBitmap(mat: Mat): Bitmap {
        // 创建一个与 Mat 对象尺寸相同的 Bitmap 对象
        val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565)

        // 将 Mat 对象的数据复制到 Bitmap 对象
        Utils.matToBitmap(mat, bitmap)

        return bitmap
    }

    @Throws(IOException::class)
    fun readImageFromInputStream(inputStream: InputStream): Mat {
        // 将 InputStream 转换为 byte[]
        inputStream.reset()
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        val imageBytes = byteArrayOutputStream.toByteArray()

        // 使用 OpenCV 解码 byte[] 为 Mat 对象
        return Imgcodecs.imdecode(MatOfByte(*imageBytes), Imgcodecs.IMREAD_COLOR)
    }

    private fun readTestInputImage(): InputStream {
        return assets.open("test_object_detection_${0}.jpg")
    }


    fun convertBitmapToMat(bitmap: Bitmap): Mat {
        // 创建一个字节输出流，用于存储 Bitmap 压缩后的字节数据
        val stream = ByteArrayOutputStream()
        // 将 Bitmap 以 JPEG 格式压缩到字节输出流中，压缩质量为 100（无损压缩）
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        // 从字节输出流中获取字节数组
        val byteArray = stream.toByteArray()
        // 将字节数组封装为 MatOfByte 对象
        val matOfByte = MatOfByte(*byteArray)
        // 使用 Imgcodecs.imdecode 方法将 MatOfByte 中的字节数据解码为 Mat 对象
        // Imgcodecs.IMREAD_COLOR 表示以彩色模式解码图像
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR)
    }

    fun convertARGBToRGB565(argbBitmap: Bitmap): Bitmap {
        // 创建一个新的 RGB565 色彩模式的 Bitmap
        val rgb565Bitmap =
            Bitmap.createBitmap(argbBitmap.width, argbBitmap.height, Bitmap.Config.RGB_565)

        // 创建一个 Canvas 对象，用于在新的 Bitmap 上绘制图像
        val canvas = Canvas(rgb565Bitmap)

        // 将原始的 ARGB Bitmap 绘制到新的 RGB565 Bitmap 上
        canvas.drawBitmap(argbBitmap, 0f, 0f, null)

        // 返回转换后的 RGB565 Bitmap
        return rgb565Bitmap
    }

    fun resizeBitmap(sourceBitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        // 获取原始 Bitmap 的宽度和高度
        val sourceWidth = sourceBitmap.width
        val sourceHeight = sourceBitmap.height

        // 计算缩放比例
        val scaleWidth = targetWidth.toFloat() / sourceWidth
        val scaleHeight = targetHeight.toFloat() / sourceHeight

        // 选择较小的缩放比例以保持宽高比
        val scaleFactor = min(scaleWidth.toDouble(), scaleHeight.toDouble()).toFloat()

        // 创建矩阵并设置缩放比例
        val matrix = Matrix()
        matrix.postScale(scaleFactor, scaleFactor)

        // 按缩放比例创建新的 Bitmap
        val scaledBitmap =
            Bitmap.createBitmap(sourceBitmap, 0, 0, sourceWidth, sourceHeight, matrix, true)

        // 创建目标尺寸的空白 Bitmap
        val targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)

        // 创建画布并将缩放后的 Bitmap 居中绘制到目标 Bitmap 上
        val canvas = Canvas(targetBitmap)
        val paint = Paint()
        val left = (targetWidth - scaledBitmap.width) / 2
        val top = (targetHeight - scaledBitmap.height) / 2
        canvas.drawBitmap(scaledBitmap, left.toFloat(), top.toFloat(), paint)

        // 回收中间产生的 Bitmap 以节省内存
        if (scaledBitmap != sourceBitmap) {
            scaledBitmap.recycle()
        }

        return targetBitmap
    }


    fun convertBitmapToInputStream(bitmap: Bitmap): InputStream {
        var outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }


    override fun onResume() {
        super.onResume()
//        glRender.onResume()
    }


    override fun onPause() {
        super.onPause()
//        glRender.onPause()

    }

}