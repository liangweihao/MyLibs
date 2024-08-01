package com.nothing.commonutils.opengl;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.HardwareBuffer;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.ParcelFileDescriptor;

import com.nothing.commonutils.utils.Lg;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;



public class EGLCoreUtils {

    private final String TAG = "EGLCoreUtils";

    private EGLCore outputEglCore = null;
    private EGLSurface outputEGLSurface = EGL14.EGL_NO_SURFACE;

    private SharedTexture sharedTexture;

    private int texId = 0;
    private int contentTextureId = 0;
    private int framebufferId = 0;

    private boolean renderReady = false;
    private HardwareBuffer buff;


    public EGLCoreUtils() {

        onCreate();
    }

    public HardwareBuffer getBuff() {
        return buff;
    }

    private static long calculateMemorySize(int width, int height, int format) {
        // 计算每个像素所占字节数
        int bytesPerPixel = ImageFormat.getBitsPerPixel(format) / 8;

        // 计算总内存大小
        long memorySize = (long) width * height * bytesPerPixel;

        return memorySize;
    }

    public void init(HardwareBuffer buff)   {
        this.buff = buff;
        outputEglCore = new EGLCore(EGL14.EGL_NO_CONTEXT, EGLCore.FLAG_TRY_GLES3);
        outputEGLSurface = outputEglCore.createOffsetScreenSurface(buff.getWidth(), buff.getHeight());
        outputEglCore.makeCurrent(outputEGLSurface);
        // create gl texture and bind to SharedTexture
        texId = EGLCore.createTexture(GLES20.GL_TEXTURE_2D);
        Lg.d(TAG, "AIDL call: init " + buff + " textID:" + texId + " bufferSize:" + calculateMemorySize(buff.getWidth(), buff.getHeight(), buff.getFormat()));

        sharedTexture = new SharedTexture(buff);
        sharedTexture.bindTexture(texId, GLES20.GL_TEXTURE_2D);
        // render to texture
        int[] framebuffers = new int[1];
        // 生成 缓冲区对象  生成 缓冲区对象标识符号
        GLES20.glGenFramebuffers(1, framebuffers, 0);
        GLES20.glGenTextures(textureHandle.length, textureHandle, 0);
        framebufferId = framebuffers[0];
        // 设置窗口的位置
//        GLES20.glViewport(0, 0, containerWidth, containerHeight);
        // 绑定 当前的缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        // 将纹理 附加到 缓冲区对象
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texId, 0);
        renderReady = true;
    }
    // bitmap -> framebufferId -> textId -> EGLDisplay -> EGLClientBuffer -> HardwareBuffer

    // 绘制完成以后通知 Unity
    public ParcelFileDescriptor drawFrame(Bitmap renderBitmap) {
        if (!renderReady) {
            return null;
        }
        outputEglCore.makeCurrent(outputEGLSurface);
        // 绑定当前的缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        // 清楚当前缓冲区的内容
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (renderBitmap != null && !renderBitmap.isRecycled()) {
            // 产生 bitmap的 纹理
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                   GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                                   GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, renderBitmap, 0);
            contentTextureId = textureHandle[0];
        } else {
            contentTextureId = 0;
        }
        // Draw image
        drawImage(contentTextureId,GLES20.GL_TEXTURE_2D);

//        int width = renderBitmap.getWidth();
//        int height = renderBitmap.getHeight();
//        ByteBuffer pixels = ByteBuffer.allocateDirect(width*height*4);
//        GLES20.glReadPixels(0,0,width,height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,pixels);
//        pixels.position(0);
//        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true);
//        newBitmap.copyPixelsFromBuffer(pixels);
        GLES20.glFlush();
        return sharedTexture.createFence();
    }

    public ParcelFileDescriptor drawFrameTexture(int textureID, int textureTarget) {
        if (!renderReady) {
            return null;
        }
        outputEglCore.makeCurrent(outputEGLSurface);
        // 绑定当前的缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        // 清楚当前缓冲区的内容
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Draw image
        drawImage(textureID,textureTarget);
        GLES20.glFlush();
        return sharedTexture.createFence();
    }

    public ParcelFileDescriptor drawFrame(Buffer buffer) {
        if (!renderReady) {
            return null;
        }
        outputEglCore.makeCurrent(outputEGLSurface);
        // 绑定当前的缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        // 清楚当前缓冲区的内容
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Draw image
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                               GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                               GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,buff.getWidth(),buff.getHeight(),0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,buffer);
        drawImage(textureHandle[0],GLES20.GL_TEXTURE_2D);
        GLES20.glFlush();
        return sharedTexture.createFence();
    }



    public void destroy()  {
        Lg.d(TAG, "AIDL call: destroy");
        if (!renderReady) {
            return;
        }

        sharedTexture.release();
        EGLCore.deleteTexture(texId);
        EGLCore.deleteTexture(contentTextureId);

        int[] frameBuffers = new int[]{framebufferId};
        GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
        onDestroy();
    }
    private void onCreate() {

        // create egl context
    }

    private void onDestroy() {
        renderReady = false;
        outputEglCore.releaseSurface(outputEGLSurface);
        outputEglCore.release();
        buff = null;
        outputEglCore = null;
        outputEGLSurface = EGL14.EGL_NO_SURFACE;

    }
    // 顶点着色器代码
    final String vertexShaderCode =
                    "attribute vec4 vPosition;" +
                    "attribute vec2 aTexCoord;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  vTexCoord = aTexCoord;" +
                    "}";

    // 片段着色器代码
    final String fragmentShaderCode =
                    "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, vTexCoord);" +
                    "}";


    private void drawImage(int textureId,int textureTarget) {
        // Define vertices for a rectangle
        float[] vertices = {
                // X, Y, U, V
                -1.0f,  1.0f, 0.0f, 0.0f, // Top left
                -1.0f, -1.0f, 0.0f, 1.0f, // Bottom left
                1.0f,  1.0f, 1.0f, 0.0f, // Top right
                1.0f, -1.0f, 1.0f, 1.0f  // Bottom right
        };
        // 设置顶点
        // Load the vertex data into a buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // Load shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Create a shader program
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        // Use the shader program
        GLES20.glUseProgram(program);

        // 分别将顶点的 四个点 放到缓冲区中 gl_Position 指定顶点的裁剪空间坐标
        // Get handle to vertex shader's vPosition member
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");

        // 颜色分量  传给片段
        // Get handle to fragment shader's vTexCoord member
        int texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");

        // 获取贴图
        int textureUniformHandle = GLES20.glGetUniformLocation(program, "uTexture");

        // Enable a handle to the rectangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Enable a handle to the texture coordinates
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        // Prepare the rectangle coordinate data
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, vertexBuffer);

        // Prepare the texture coordinate data
        vertexBuffer.position(2);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, vertexBuffer);

        // Bind the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(textureTarget, textureId);

        // Set the sampler to texture unit 0
        GLES20.glUniform1i(textureUniformHandle, 0);

        // Draw the rectangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertices.length/4);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
    final int[] textureHandle = new int[1];

    private int loadTexture(Bitmap bitmap) {
        if (textureHandle[0] != 0) {

         }
        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }
        return textureHandle[0];
    }

//    private int loadTexture(ByteBuffer buffer){
//
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,pixelFormat,imageWidth,imageHeight,0,);
//    }


    // Method to load shaders
    private int loadShader(int type, String shaderCode){
        // Create a shader
        int shader = GLES20.glCreateShader(type);

        // Add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
