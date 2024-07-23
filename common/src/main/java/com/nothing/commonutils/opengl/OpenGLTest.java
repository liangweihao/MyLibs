package com.nothing.commonutils.opengl;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.OpenGlWrapper;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_RGBA4;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glReadPixels;

public class OpenGLTest {


    /**
     * 多看看 API： https://registry.khronos.org/OpenGL-Refpages/es2.0/
     * 太难了 终于搞出来了遇到了很多问题
     * 1.EGLDisplay无效 检查 EGL14.eglInitialize 是否执行
     * 2.glUseProgram 502 检查glCompileShader/glAttachShader/glLinkProgram是否执行
     * 3.glFramebufferRenderbuffer 相关的错误 检查 glBindRenderbuffer/glBindFramebuffer/glRenderbufferStorage
     * 4.像素格式 glReadPixels的时候像素要保持一致
     */
    public static void 离屏渲染单线程模拟绘制三角形() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EGLDisplay mEglDisplay;
                EGLContext eglContext;

                boolean contextCreated = false;
                int surfaceWidth = 1920;
                int surfaceHeight = 1080;
                int glCreateProgram = 0;
                while (true) {
                    if (!contextCreated) {
                        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
                        boolean eglInitialize = EGL14.eglInitialize(mEglDisplay, null, 0, null, 0);
                        int[] num_config = new int[1];
                        int[] config_attr_list
                                = new int[]{EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_NONE};
                        EGLConfig[] elgConfigs = new EGLConfig[1];
                        EGL14.eglChooseConfig(mEglDisplay, config_attr_list, 0, elgConfigs, 0, 1,
                                              num_config, 0);

                        int[] context_attrib_list
                                = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

                        eglContext = EGL14.eglCreateContext(mEglDisplay, elgConfigs[0],
                                                            EGL14.EGL_NO_CONTEXT,
                                                            context_attrib_list, 0);


                        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(mEglDisplay,
                                                                              elgConfigs[0],
                                                                              new int[]{EGL14.EGL_WIDTH, surfaceWidth, EGL14.EGL_HEIGHT, surfaceHeight, EGL14.EGL_NONE},
                                                                              0);

                        contextCreated = EGL14.eglMakeCurrent(mEglDisplay, eglSurface, eglSurface,
                                                              eglContext);
                        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);

                        glCreateProgram = GLES20.glCreateProgram();

                        int glVertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                        int glFragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                        GLES20.glShaderSource(glVertexShader,
                                              "attribute vec2 inputPosition;" + "void main(){" +
                                              "   gl_Position = vec4(inputPosition, 0.0, 1.0);" +
                                              "}");
                        GLES20.glShaderSource(glFragmentShader,
                                              "precision mediump float; " + "void main(){" +
                                              "   gl_FragColor = vec4(0.3,1,0.4,1);" + "}");
                        GLES20.glCompileShader(glVertexShader);
                        GLES20.glCompileShader(glFragmentShader);
                        GLES20.glAttachShader(glCreateProgram, glVertexShader);
                        GLES20.glAttachShader(glCreateProgram, glFragmentShader);
                        GLES20.glLinkProgram(glCreateProgram);
                    } else {
                        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                        GLES20.glUseProgram(glCreateProgram);
                        int[] renderbuffers = new int[1];
                        GLES20.glGenRenderbuffers(1, renderbuffers, 0);
                        int[] framebuffers = new int[1];
                        GLES20.glGenFramebuffers(1, framebuffers, 0);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0]);
                        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_RGBA4,
                                                     surfaceWidth, surfaceHeight);
                        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                                                         GLES20.GL_COLOR_ATTACHMENT0,
                                                         GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);


                        int inputPositionIndex = GLES20.glGetAttribLocation(glCreateProgram,
                                                                            "inputPosition");
                        GLES20.glEnableVertexAttribArray(inputPositionIndex);
                        float[] vertexArray
                                = new float[]{-0.8f, 0.8f, 0.8f, 0.8f, 0.8f, -0.8f, -0.8f, -0.8f, -0.8f, 0.8f,

//                                -1.0f / 2, -1.0f, +1.0f, -1.0f, +1.0f,
//                                +1.0f / 2, +1.0f / 2, +1.0f, -1.0f / 2, +1.0f, -1.0f, -1.0f
                        };

                        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(vertexArray.length * 4)
                                                            .order(ByteOrder.nativeOrder())
                                                            .asFloatBuffer();
                        floatBuffer.put(vertexArray);
                        floatBuffer.position(0);
                        GLES20.glVertexAttribPointer(inputPositionIndex, 2, GLES20.GL_FLOAT, false,
                                                     0, floatBuffer);

                        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexArray.length / 2);

                        Buffer pixels = ByteBuffer.allocateDirect(surfaceWidth * surfaceHeight * 4);
                        ;
                        glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA,
                                     GLES20.GL_UNSIGNED_BYTE, pixels);
                        Bitmap bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                                            Bitmap.Config.ARGB_8888, true);
                        pixels.rewind();
                        bitmap.copyPixelsFromBuffer(pixels);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
        });

        thread.start();
    }

    /**
     * 遗留问题 ： 每次刷新的时候 绘制的内容都不一样 感觉像是绘制的步骤一步一步的 每次读取的内容都不一样 不知道该如何做到同步呢？
     * 踩坑啊：ByteBuffer.allocateDirect(vertexPoints.length * 4)
     * .order(ByteOrder.nativeOrder())
     * .asFloatBuffer()
     * .put(vertexPoints); 没有进行 position(0) 的复位操作
     * 踩坑：
     * glGenBuffers / glBindBuffer / glBufferData / glEnableVertexAttribArray / glVertexArrayPointer /glDrawArray
     * 绘制完毕以后发现 没有内容 最终发现 glBufferData的size 没有设置对  例如 width * height * 4 / buffer.capacity() * 4
     */
    public static void 离屏渲染绘制一个矩形() {


        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                int[] framebuffers = new int[1];
                int[] renderbuffers = new int[1];
                int[] buffers = new int[1];
                float[] vertexPoints
                        = new float[]{-0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f

                };
                int surfaceWidth = 1920;
                int surfaceHeight = 1080;
                int glProgram = 0;
                boolean elgContextCreate = false;
                // 轮播每一帧
                while (true) {

                    if (!elgContextCreate) {

                        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
                        boolean eglInitialize = EGL14.eglInitialize(eglDisplay, null, 0, null, 0);


                        int[] choose_config_attrib
                                = new int[]{EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_NONE};
                        EGLConfig[] eglConfigs = new EGLConfig[1];
                        boolean eglChooseConfig = EGL14.eglChooseConfig(eglDisplay,
                                                                        choose_config_attrib, 0,
                                                                        eglConfigs, 0,
                                                                        eglConfigs.length,
                                                                        new int[eglConfigs.length],
                                                                        0);


                        int[] context_attrib
                                = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
                        EGLContext eglContext = EGL14.eglCreateContext(eglDisplay, eglConfigs[0],
                                                                       EGL14.EGL_NO_CONTEXT,
                                                                       context_attrib, 0);

                        int[] surface_attrib
                                = new int[]{EGL14.EGL_WIDTH, surfaceWidth, EGL14.EGL_HEIGHT, surfaceHeight, EGL14.EGL_NONE};
                        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay,
                                                                              eglConfigs[0],
                                                                              surface_attrib, 0);
                        boolean eglMakeCurrent = EGL14.eglMakeCurrent(eglDisplay, eglSurface,
                                                                      eglSurface, eglContext);

                        int eglGetError = EGL14.eglGetError();
                        if (eglGetError == EGL14.EGL_SUCCESS) {
                            elgContextCreate = true;
                        }

                        glProgram = GLES20.glCreateProgram();


                        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                        GLES20.glShaderSource(vertexShader,
                                              "attribute vec2 inputPoint;" + "void main(){" +
                                              "   gl_Position = vec4(inputPoint,1f,1f);" + "" + "" +
                                              "}");
                        GLES20.glShaderSource(fragmentShader,
                                              "precision mediump float;" + "void main(){" +
                                              "   gl_FragColor = vec4(0.3,0.3,0.8,1.0);" + "" +
                                              "}");
                        GLES20.glAttachShader(glProgram, vertexShader);
                        GLES20.glAttachShader(glProgram, fragmentShader);
                        GLES20.glCompileShader(vertexShader);
                        GLES20.glCompileShader(fragmentShader);
                        GLES20.glLinkProgram(glProgram);
                        GLES20.glGenFramebuffers(1, framebuffers, 0);
                        GLES20.glGenRenderbuffers(1, renderbuffers, 0);
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0]);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_RGBA4,
                                                     surfaceWidth, surfaceHeight);
                        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                                                         GLES20.GL_COLOR_ATTACHMENT0,
                                                         GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
                        int glGetError = GLES20.glGetError();
                        String eglErrorString = GLUtils.getEGLErrorString(glGetError);


                        GLES20.glGenBuffers(1, buffers, 0);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

                        Buffer vertexBuffer = ByteBuffer.allocateDirect(vertexPoints.length * 4)
                                                        .order(ByteOrder.nativeOrder())
                                                        .asFloatBuffer()
                                                        .put(vertexPoints)
                                                        .position(0);
                        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4,
                                            vertexBuffer, GLES20.GL_STATIC_DRAW);

                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                    } else {

                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                        GLES20.glClearColor(0.4f, 0.8f, 0.7f, 1f);

                        GLES20.glUseProgram(glProgram);

                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0]);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        int inputPointIndex = GLES20.glGetAttribLocation(glProgram, "inputPoint");
                        GLES20.glEnableVertexAttribArray(inputPointIndex);

//                        Buffer vertexBuffer = ByteBuffer.allocateDirect(vertexPoints.length * 4)
//                                                        .order(ByteOrder.nativeOrder())
//                                                        .asFloatBuffer()
//                                                        .put(vertexPoints).position(0);
//
//                        GLES20.glVertexAttribPointer(inputPointIndex, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                        GLES20.glVertexAttribPointer(inputPointIndex, 2, GLES20.GL_FLOAT, false, 0,
                                                     0);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


                        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexPoints.length / 2);
//                        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP,vertexPoints.length/2,GLES20.GL_UNSIGNED_SHORT,0);


                        Buffer pixels = IntBuffer.allocate(surfaceWidth * surfaceHeight);
                        glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA,
                                     GLES20.GL_UNSIGNED_BYTE, pixels);
                        Bitmap bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                                            Bitmap.Config.ARGB_8888, true);
                        pixels.rewind();
                        bitmap.copyPixelsFromBuffer(pixels);
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
                        int eglGetError = EGL14.eglGetError();
                        GLES20.glDisableVertexAttribArray(inputPointIndex);
                        GLES20.glDeleteProgram(glProgram);
                        try {
                            Thread.sleep(160);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                // 创建上下文


            }
        };
        thread.start();
    }

    private static final String TAG = "OpenGLTest";

    /**
     * 踩坑；
     * 使用 bufferdata 完毕以后直接使用了 glDisableVertexAttribArray 导致 drawArrays 没有任何效果
     * 在glDrawArrays 之前就进行了 glBindTexture(GLES20.GL_TEXTURE_2D,0); 导致 绘制图片失效了
     * <p>
     * 怎么用？？？
     * GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
     * GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
     * GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
     * GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
     * texVertex 假如 这个顶点的数据 大于 1 就开始生效纹理的拉伸规则了
     * 知识点：
     * glBindFramebuffer 设置新的 framebuffer 绑定到帧 buffer 名字上 并且会把之前绑定的释放，这就导致了当你绑定了一个 buffer 以后 在执行了一次glBindFramebuffer就会把 buffer 内容清理了 此时如果不尽兴
     * drawArray行为的话就会导致缓冲区的图像没有内容了， 注意了 glBindRenderbuffer 再次绑定实际上不会影响到 glReadPixels 的行为 我理解因为 内容已经写到了缓冲区中了
     * glBindRenderbuffer的生命周期。 glBindRenderbuffer绑定了另外一个 name 但是不会清空 buffer 或者是通过glDeleteRenderbuffers 删除了 同理  glDeleteFramebuffers.也是这个样子
     */
    public static void 离屏渲染题图四边形(Bitmap bitmap) {
        Thread thread = new Thread() {
            boolean contextCreated = false;

            int surfaceWidth = 1920;
            int surfaceHeight = 1080;
            int program = 0;
            int[] buffers = new int[2];
            int picPositionIndex = 0;
            int inputTexPositionIndex = 0;
            String eglErrorString = "";
            float[] pointVertex = new float[]{-1f, 1f, 1f, 1f, 1f, -1f, -1f, -1f};

            float[] texVertex = new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};
            int[] textures = new int[1];
            int[] framebuffers = new int[1];
            int[] renderbuffers = new int[1];

            String vShader = "attribute vec2 picPosition;" + "attribute vec2 inputTexPosition;" +
                             "varying vec2 texPosition;" + "void main(){" +
                             "    gl_Position = vec4(picPosition,1.0,1.0);" +
                             "    texPosition = inputTexPosition;" + "}";

            String fShader = "precision mediump float;" + "uniform sampler2D inputTexture;" +
                             "varying vec2 texPosition;" + "void main(){" +
                             "      gl_FragColor = texture2D(inputTexture,texPosition);" +
                             "}";

            @Override
            public void run() {
                super.run();


                while (true) {

                    if (!contextCreated) {

                        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
                        boolean eglInitialize = EGL14.eglInitialize(eglDisplay, null, 0, null, 0);


                        int[] config_attrib_list
                                = new int[]{EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_NONE};
                        EGLConfig[] eglConfigs = new EGLConfig[1];
                        boolean eglChooseConfig = EGL14.eglChooseConfig(eglDisplay,
                                                                        config_attrib_list, 0,
                                                                        eglConfigs, 0, 1,
                                                                        new int[eglConfigs.length],
                                                                        0);
                        EGLConfig eglConfig = eglConfigs[0];
                        int[] surface_attrib_list
                                = new int[]{EGL14.EGL_WIDTH, surfaceWidth, EGL14.EGL_HEIGHT, surfaceHeight, EGL14.EGL_NONE};
                        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig,
                                                                              surface_attrib_list,
                                                                              0);
                        int[] context_attrib_list
                                = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
                        EGLContext eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig,
                                                                       EGL14.EGL_NO_CONTEXT,
                                                                       context_attrib_list, 0);
                        boolean eglMakeCurrent = EGL14.eglMakeCurrent(eglDisplay, eglSurface,
                                                                      eglSurface, eglContext);

                        if (eglMakeCurrent) {
                            contextCreated = true;
                        }

                        int eglGetError = EGL14.eglGetError();

                        eglErrorString = GLUtils.getEGLErrorString(eglGetError);


                        program = GLES20.glCreateProgram();

                        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
                        GLES20.glShaderSource(vertexShader, vShader);
                        GLES20.glAttachShader(program, vertexShader);
                        GLES20.glCompileShader(vertexShader);

                        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
                        GLES20.glShaderSource(fragmentShader, fShader);
                        GLES20.glAttachShader(program, fragmentShader);
                        GLES20.glCompileShader(fragmentShader);
                        GLES20.glLinkProgram(program);
                        GLES20.glGenBuffers(2, buffers, 0);

                        picPositionIndex = GLES20.glGetAttribLocation(program, "picPosition");
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pointVertex.length * 4,
                                            ByteBuffer.allocateDirect(pointVertex.length * 4)
                                                      .order(ByteOrder.nativeOrder())
                                                      .asFloatBuffer()
                                                      .put(pointVertex)
                                                      .position(0), GLES20.GL_STATIC_DRAW);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


                        inputTexPositionIndex = GLES20.glGetAttribLocation(program,
                                                                           "inputTexPosition");
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texVertex.length * 4,
                                            ByteBuffer.allocateDirect(texVertex.length * 4)
                                                      .order(ByteOrder.nativeOrder())
                                                      .asFloatBuffer()
                                                      .put(texVertex)
                                                      .position(0), GLES20.GL_STATIC_DRAW);


                        GLES20.glGenFramebuffers(1, framebuffers, 0);
                        GLES20.glGenRenderbuffers(1, renderbuffers, 0);
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0]);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        // 更新渲染缓冲区的参数
                        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GL_RGBA4, surfaceWidth,
                                                     surfaceHeight);
                        // 首先获取 GL_FRAMEBUFFER 获取帧缓冲区 然后根据渲染缓冲区得到渲染对象 并且通过渲染对象得到采样， 然后将渲染的对象和采样 附加到帧缓冲区
                        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                                                         GLES20.GL_COLOR_ATTACHMENT0,
                                                         GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);

                        GLES20.glGenTextures(1, textures, 0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                                               GLES20.GL_MIRRORED_REPEAT);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                                               GLES20.GL_MIRRORED_REPEAT);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                               GLES20.GL_LINEAR);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                                               GLES20.GL_LINEAR);
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
                        int inputTextureIndex = GLES20.glGetUniformLocation(program,
                                                                            "inputTexture");
                        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES20.glUniform1i(inputTextureIndex, 0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

                        // 空间顶点
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                        GLES20.glEnableVertexAttribArray(picPositionIndex);
                        GLES20.glVertexAttribPointer(picPositionIndex, 2, GLES20.GL_FLOAT, false, 0,
                                                     0);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                        GLES20.glDisableVertexAttribArray(picPositionIndex);

                        // 纹理顶点
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                        GLES20.glEnableVertexAttribArray(inputTexPositionIndex);
                        GLES20.glVertexAttribPointer(inputTexPositionIndex, 2, GLES20.GL_FLOAT,
                                                     false, 0, 0);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                        GLES20.glDisableVertexAttribArray(inputTexPositionIndex);

                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);
                        eglErrorString = GLES20.glGetProgramInfoLog(program);
                        eglErrorString = GLUtils.getEGLErrorString(GLES20.glGetError());
                        Lg.d(TAG, " egl error %s", eglErrorString);

                    } else {
                        EGLContext eglGetCurrentContext = EGL14.eglGetCurrentContext();

                        GLES20.glUseProgram(program);
                        GLES20.glEnableVertexAttribArray(picPositionIndex);
                        GLES20.glEnableVertexAttribArray(inputTexPositionIndex);

                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

                        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, pointVertex.length / 2);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

                        Buffer pixels = IntBuffer.allocate(surfaceWidth * surfaceHeight);
                        GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA,
                                            GLES20.GL_UNSIGNED_BYTE, pixels);
                        Bitmap bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                                            Bitmap.Config.ARGB_8888, false);
                        bitmap.copyPixelsFromBuffer(pixels.rewind());
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);


                        GLES20.glDeleteProgram(program);
                        GLES20.glReleaseShaderCompiler();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                }
            }
        };


        thread.start();
    }


    public static int glGet(int pname) {
        int[] params = new int[1];
        GLES20.glGetIntegerv(pname, params, 0);
        return params[0];
    }

    public static int[] glGetBuffer(int target, int pname) {
        int[] params = new int[2];
        GLES20.glGetBufferParameteriv(target, pname, params, 0);
        return params;
    }

    public static int[] glGetUniformiv(int program, int location) {
        int[] params = new int[2];
        GLES20.glGetUniformiv(program, location, params, 0);
        return params;
    }

    public static int[] glGetProgramiv(int program, int pname) {
        int[] params = new int[2];
        GLES20.glGetProgramiv(program, pname, params, 0);
        return params;
    }


    /**
     * 坑： 创建着色器的时候 没有进行glCreateShader 的方式
     * glCreateProgram 的时候 会报 501 实际上正常的程序也会报
     * GLES20.glGetBufferParameteriv(GLES20.GL_ARRAY_BUFFER, GLES20.GL_BUFFER_SIZE, arrayBufferParams, 0); 测量是的对象的 bytes
     * 踩坑：glVertexAttribPointer size：应该是数据的长度 x,y 应该是 2 否则就 导致绘制不出来
     */
    // 矩阵的应用
    public static void 离屏渲染题图正方向居中裁剪_MAT(Bitmap bitmap) {

        Thread thread = new Thread() {


            @Override
            public void run() {
                super.run();
                int vexPositionIndex = 0;
                int inputTexturePositionIndex = 0;
                boolean contextCreated = false;
                int program = 0;
                int surfaceWidth = 1920;
                int surfaceHeight = 1080;
                String vertexSource = "attribute vec2 vexPosition;" +
                                      "attribute vec2 inputTexturePosition;" +
                                      "varying vec2 texPosition;" + "void main(){" +
                                      "     gl_Position = vec4(vexPosition,1.0,1.0);" +
                                      "     texPosition = inputTexturePosition;" + "}";

                String fragmentSource = "precision mediump float;" +
                                        "uniform sampler2D textureSampler;" +
                                        "varying vec2 texPosition;" + "void main(){" +
                                        "     gl_FragColor = texture2D(textureSampler,texPosition);" +
                                        "" + "" + "}";

                String infoLog = "";
                int[] framebuffers = new int[1];
                int[] renderbuffers = new int[1];
                int[] buffers = new int[2];
                int[] textures = new int[1];
                // 纹理缓冲区
                float[] texPosition = new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};
                // 顶点的位置 左上角
                RectF positionRect = new RectF(-0.8f, 0.8f, 0.8f, -0.8f);


                Matrix matrix = new Matrix();
                matrix.setScale(4, 4);
                matrix.mapRect(positionRect);

                float[] vexPosition
                        = new float[]{positionRect.left, positionRect.top, positionRect.right, positionRect.top, positionRect.right, positionRect.bottom, positionRect.left, positionRect.bottom};

                while (true) {

                    if (!contextCreated) {

                        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
                        boolean eglInitialize = EGL14.eglInitialize(eglDisplay, null, 0, null, 0);


                        int[] config_attrib_list
                                = new int[]{EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_NONE};
                        EGLConfig[] eglConfigs = new EGLConfig[1];
                        boolean eglChooseConfig = EGL14.eglChooseConfig(eglDisplay,
                                                                        config_attrib_list, 0,
                                                                        eglConfigs, 0, 1,
                                                                        new int[eglConfigs.length],
                                                                        0);
                        EGLConfig eglConfig = eglConfigs[0];
                        int[] surface_attrib_list
                                = new int[]{EGL14.EGL_WIDTH, surfaceWidth, EGL14.EGL_HEIGHT, surfaceHeight, EGL14.EGL_NONE};
                        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig,
                                                                              surface_attrib_list,
                                                                              0);
                        int[] context_attrib_list
                                = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
                        EGLContext eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig,
                                                                       EGL14.EGL_NO_CONTEXT,
                                                                       context_attrib_list, 0);
                        boolean eglMakeCurrent = EGL14.eglMakeCurrent(eglDisplay, eglSurface,
                                                                      eglSurface, eglContext);
                        if (eglMakeCurrent) {
                            contextCreated = true;
                        } else {
                            int eglGetError = EGL14.eglGetError();
                            Lg.d(TAG, "egl 14 error : %d ", eglGetError);
                        }


                        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
                        program = GLES20.glCreateProgram();

                        int vShaderID = GLES20.glCreateShader(GL_VERTEX_SHADER);
                        int fShaderID = GLES20.glCreateShader(GL_FRAGMENT_SHADER);
                        GLES20.glShaderSource(vShaderID, vertexSource);

                        GLES20.glShaderSource(fShaderID, fragmentSource);

                        GLES20.glAttachShader(program, vShaderID);
                        GLES20.glCompileShader(vShaderID);

                        GLES20.glAttachShader(program, fShaderID);
                        GLES20.glCompileShader(fShaderID);

                        GLES20.glLinkProgram(program);

                        infoLog = GLES20.glGetProgramInfoLog(program);

                        vexPositionIndex = GLES20.glGetAttribLocation(program, "vexPosition");


                        inputTexturePositionIndex = GLES20.glGetAttribLocation(program,
                                                                               "inputTexturePosition");
                        GLES20.glGenFramebuffers(framebuffers.length, framebuffers, 0);
                        GLES20.glGenRenderbuffers(renderbuffers.length, renderbuffers, 0);
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, framebuffers[0]);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, renderbuffers[0]);

                        GLES20.glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA4, surfaceWidth,
                                                     surfaceHeight);

                        GLES20.glFramebufferRenderbuffer(GL_FRAMEBUFFER,
                                                         GLES20.GL_COLOR_ATTACHMENT0,
                                                         GL_RENDERBUFFER, renderbuffers[0]);
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);

                        // 顶点缓冲区
                        GLES20.glGenBuffers(buffers.length, buffers, 0);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

                        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vexPosition.length * 4,
                                            ByteBuffer.allocateDirect(vexPosition.length * 4)
                                                      .order(ByteOrder.nativeOrder())
                                                      .asFloatBuffer()
                                                      .put(vexPosition)
                                                      .rewind(), GLES20.GL_STATIC_DRAW);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texPosition.length * 4,
                                            ByteBuffer.allocateDirect(texPosition.length * 4)
                                                      .order(ByteOrder.nativeOrder())
                                                      .asFloatBuffer()
                                                      .put(texPosition)
                                                      .rewind(), GLES20.GL_STATIC_DRAW);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


                        GLES20.glEnableVertexAttribArray(vexPositionIndex);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                        GLES20.glVertexAttribPointer(vexPositionIndex, 2, GLES20.GL_FLOAT, false, 0,
                                                     0);
                        GLES20.glDisableVertexAttribArray(vexPositionIndex);

                        GLES20.glEnableVertexAttribArray(inputTexturePositionIndex);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                        GLES20.glVertexAttribPointer(inputTexturePositionIndex, 2, GLES20.GL_FLOAT,
                                                     false, 0, 0);
                        GLES20.glDisableVertexAttribArray(inputTexturePositionIndex);


                        GLES20.glGenTextures(textures.length, textures, 0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                                               GLES20.GL_MIRRORED_REPEAT);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                                               GLES20.GL_MIRRORED_REPEAT);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                               GLES20.GL_LINEAR);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                                               GLES20.GL_LINEAR);
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);

                        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "textureSampler"),
                                           0);


                    } else {
                        GLES20.glUseProgram(program);


                        GLES20.glEnableVertexAttribArray(vexPositionIndex);
                        GLES20.glEnableVertexAttribArray(inputTexturePositionIndex);

                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, framebuffers[0]);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, renderbuffers[0]);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vexPosition.length / 2);


                        Buffer pixels = IntBuffer.allocate(surfaceWidth * surfaceHeight);
                        GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA,
                                            GLES20.GL_UNSIGNED_BYTE, pixels);

                        Bitmap newBitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                                               Bitmap.Config.ARGB_8888, false);
                        newBitmap.copyPixelsFromBuffer(pixels.rewind());
                        GLES20.glDisableVertexAttribArray(vexPositionIndex);
                        GLES20.glDisableVertexAttribArray(inputTexturePositionIndex);


                        try {
                            Thread.sleep(160);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };

        thread.start();

    }

    // 基于 GLSL 的算法
    public static void 离屏渲染题图正方向居中裁剪_GLSL() {

    }

    public static float[] rectToFloatArray(RectF rectF) {
        float[] temp
                = new float[]{rectF.left, rectF.bottom, rectF.right, rectF.bottom, rectF.right, rectF.top, rectF.left, rectF.bottom};
        return temp;
    }

    public static int[] glGetShaderiv(int shader, int pname) {
        int[] result_array = new int[2];
        GLES20.glGetShaderiv(shader, pname, result_array, 0);
        return result_array;
    }


    /**
     * glVertexAttribPointer size 又踩坑了 应该是 2 x和 y
     * 默认 glActiveTexture 就是 0 所以即使不主动绑定也没事
     * 踩坑：如果 attribute 设置的变量 如果么有使用就会被丢弃 location 返回的-1
     * GLES20.glLinkProgram(programBackground);绑定两个一个非 Program是
     * 非常的奇怪                 int programOver = 0; 这个变量一声明就出现 glShaderSource -》 0x501 改成programOver2都没事
     */
    // 实现将一个图片附着在另外一个图片的右下角 输出合成后的照片
    public static void 贴图功能_附着到图片右下角XXX(Bitmap bitmap, Bitmap over) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                int program = 0;
                int surfaceWidth = 1920;
                int surfaceHeight = 1080;
                int[] textures = new int[1];
                int[] framebuffers = new int[1];
                int[] renderbuffers = new int[1];
                boolean contextCreated = false;
                String vSource = "attribute vec2 vPosition;" +
                                 "attribute vec2 vTex;" +
                                 "varying vec2 aTex;" +
                                 "void main(){" +
                                 "     gl_Position = vec4(vPosition,1.0,1.0); " +
                                 "     aTex = vTex; " +
                                 "}";
                String fSource = "precision mediump float;" +
                                 "varying vec2 aTex;" +
                                 "uniform sampler2D uTex;" +
                                 "void main(){" +
                                 "     gl_FragColor = texture2D(uTex,aTex);" +
                                 "}";

                vSource = "attribute vec2 vexPosition;" +
                                      "attribute vec2 inputTexturePosition;" +
                                      "varying vec2 texPosition;" + "void main(){" +
                                      "     gl_Position = vec4(vexPosition,1.0,1.0);" +
                                      "     texPosition = inputTexturePosition;" + "}";

                fSource = "precision mediump float;" +
                                        "uniform sampler2D textureSampler;" +
                                        "varying vec2 texPosition;" + "void main(){" +
                                        "     gl_FragColor = texture2D(textureSampler,texPosition);" +
                                        "" + "" + "}";

                float[] vPositionArray = new float[]{
                        -1,1,
                        1,1,
                        1,-1,
                        -1,-1
                };
                float[] vTexArray = new float[]{
                        0,0,
                        0,1,
                        1,1,
                        1,0
                };
                while (true) {
                    if (!contextCreated) {
                        boolean initEGLContext = OpenGlWrapper.initEGLContext(surfaceWidth, surfaceHeight);
                        if (initEGLContext) {
                            contextCreated = true;
                        } else {
                            continue;
                        }

                        program = GLES20.glCreateProgram();


                        // 创建着色器
                        int vShader = GLES20.glCreateShader(GL_VERTEX_SHADER);
                        GLES20.glShaderSource(vShader, vSource);
                        GLES20.glCompileShader(vShader);
                        GLES20.glAttachShader(program, vShader);
                        int fShader = GLES20.glCreateShader(GL_FRAGMENT_SHADER);
                        GLES20.glShaderSource(fShader, fSource);
                        GLES20.glCompileShader(fShader);
                        GLES20.glAttachShader(program, fShader);
                        GLES20.glLinkProgram(program);
                        GLES20.glUseProgram(program);


                        // 填充顶点数据
                        int vPositionIndex = GLES20.glGetAttribLocation(program, "vexPosition");
                        int vTexIndex = GLES20.glGetAttribLocation(program, "inputTexturePosition");
                        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "textureSampler"), 0);
                        GLES20.glEnableVertexAttribArray(vPositionIndex);
                        GLES20.glEnableVertexAttribArray(vTexIndex);
                        GLES20.glVertexAttribPointer(vPositionIndex, 2, GLES20.GL_FLOAT, false, 0,
                                                     ByteBuffer.allocateDirect(
                                                                       vPositionArray.length * 4)
                                                               .order(ByteOrder.nativeOrder())
                                                               .asFloatBuffer()
                                                               .put(vPositionArray)
                                                               .rewind());
                        GLES20.glVertexAttribPointer(vTexIndex, 2, GLES20.GL_FLOAT, false, 0,
                                                     ByteBuffer.allocateDirect(
                                                                       vTexArray.length * 4)
                                                               .order(ByteOrder.nativeOrder())
                                                               .asFloatBuffer()
                                                               .put(vTexArray)
                                                               .rewind());
                        //初始化纹理
                        GLES20.glGenTextures(textures.length,textures,0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,bitmap,0);


                        // 初始化帧缓冲区

                        GLES20.glGenFramebuffers(framebuffers.length,framebuffers,0);
                        GLES20.glGenFramebuffers(renderbuffers.length,renderbuffers,0);
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER,framebuffers[0]);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER,renderbuffers[0]);
                        GLES20.glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA4,surfaceWidth,surfaceHeight);
                        GLES20.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                                                         GL_RENDERBUFFER,renderbuffers[0]);

                    } else {


                        // 绘制内容

                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER,framebuffers[0]);
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0, vPositionArray.length/2);
                        Bitmap newBitmap = readBitmap(surfaceWidth, surfaceHeight);
                        try {
                            Thread.sleep(160);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }

                }
            }
        };
        thread.start();

    }

    public static Bitmap readBitmap(int surfaceWidth, int surfaceHeight) {
        Buffer pixels = IntBuffer.allocate(surfaceWidth * surfaceHeight);
        GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA,
                            GLES20.GL_UNSIGNED_BYTE, pixels);
        Bitmap newBitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                               Bitmap.Config.ARGB_8888, false);
        newBitmap.copyPixelsFromBuffer(pixels.rewind());
        return newBitmap;
    }


    public static void 贴图功能_上一个一直501_右下角(Bitmap bitmap, Bitmap over) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean contextCreated = false;
                while (true) {

                }
            }
        }).start();

    }

    public static void 贴图功能_合并图片左边一个右边一个() {


    }

    public static void 相机旋转() {

    }

    public static void 图像处理() {

    }


}
