package com.nothing.commonutils.opengl;


import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.nothing.commonutils.utils.Lg;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_RGBA4;
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
     *      使用 bufferdata 完毕以后直接使用了 glDisableVertexAttribArray 导致 drawArrays 没有任何效果
     *      在glDrawArrays 之前就进行了 glBindTexture(GLES20.GL_TEXTURE_2D,0); 导致 绘制图片失效了
     *
     * 怎么用？？？
     *                           GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
     *                         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
     *                         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
     *                         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
     *                         texVertex 假如 这个顶点的数据 大于 1 就开始生效纹理的拉伸规则了
     * */
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
            float[] pointVertex
                    = new float[]{-0.8f, 0.8f, 0.8f, 0.8f, 0.8f, -0.8f, -0.8f, -0.8f };

            float[] texVertex = new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f };
            int[] textures = new int[1];
            int[] framebuffers = new int[1];
            int[] renderbuffers = new int[1];

            String vShader = "attribute vec2 picPosition;" +
                             "attribute vec2 inputTexPosition;" +
                             "varying vec2 texPosition;" +
                             "void main(){" +
                             "    gl_Position = vec4(picPosition,1.0,1.0);" +
                             "    texPosition = inputTexPosition;" +
                             "}";

            String fShader = "precision mediump float;" +
                             "uniform sampler2D inputTexture;" +
                             "varying vec2 texPosition;" +
                             "void main(){" +
                             "      gl_FragColor = texture2D(inputTexture,texPosition);" +
//                             "      gl_FragColor = vec4(0.5,0.8,0.1,1.0);" +
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
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,framebuffers[0]);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,renderbuffers[0]);
                        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GL_RGBA4,surfaceWidth,surfaceHeight);
                        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_RENDERBUFFER,renderbuffers[0]);
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER,0);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER,0);

                        eglErrorString = GLES20.glGetProgramInfoLog(program);
                        eglErrorString = GLUtils.getEGLErrorString(GLES20.glGetError());
                        Lg.d(TAG, " egl error %s", eglErrorString);

                    } else {
                        EGLContext eglGetCurrentContext = EGL14.eglGetCurrentContext();

                        GLES20.glUseProgram(program);
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER,framebuffers[0]);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, renderbuffers[0]);


                        // 空间顶点
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
                        GLES20.glEnableVertexAttribArray(picPositionIndex);
                        GLES20.glVertexAttribPointer(picPositionIndex, 2, GLES20.GL_FLOAT, false, 0,
                                                     0);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


                        // 纹理顶点
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
                        GLES20.glEnableVertexAttribArray(inputTexPositionIndex);
                        GLES20.glVertexAttribPointer(inputTexPositionIndex, 2, GLES20.GL_FLOAT,
                                                     false, 0, 0);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

                        GLES20.glGenTextures(1, textures, 0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
                        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,bitmap,0);
                        int inputTextureIndex = GLES20.glGetUniformLocation(program, "inputTexture");
                        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES20.glUniform1i(inputTextureIndex,0);

                        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,pointVertex.length/2);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);


                        Buffer pixels = IntBuffer.allocate(surfaceWidth * surfaceHeight);
                        GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels);
                        Bitmap bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                                            Bitmap.Config.ARGB_8888, false);
                        bitmap.copyPixelsFromBuffer(pixels.rewind());
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER,0);
                        GLES20.glBindRenderbuffer(GL_RENDERBUFFER, 0);
                        GLES20.glDisableVertexAttribArray(picPositionIndex);
                        GLES20.glDisableVertexAttribArray(inputTexPositionIndex);

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


    public static void 离屏渲染题图正方向居中裁剪() {


    }

    public static void 贴图功能() {


    }


    public static void 图像处理() {

    }


}
