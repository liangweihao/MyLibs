package com.nothing.commonutils.opengl;


import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;

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
     *      踩坑啊：ByteBuffer.allocateDirect(vertexPoints.length * 4)
     *                                                         .order(ByteOrder.nativeOrder())
     *                                                         .asFloatBuffer()
     *                                                         .put(vertexPoints); 没有进行 position(0) 的复位操作
     */
    public static void 离屏渲染绘制一个矩形() {


        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                int[] framebuffers = new int[1];
                int[] renderbuffers = new int[1];

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
                        int glGetError = GLES20.glGetError();
                        String eglErrorString = GLUtils.getEGLErrorString(glGetError);

                    } else {

                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                        GLES20.glClearColor(0.4f, 0.8f, 0.7f, 1f);

                        GLES20.glUseProgram(glProgram);


                        int inputPointIndex = GLES20.glGetAttribLocation(glProgram,"inputPoint");
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffers[0]);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbuffers[0]);
                        float[] vertexPoints
                                = new float[]{-0.5f,0.5f,
                                0.5f,0.5f,
                                0.5f,-0.5f,
                                -0.5f,-0.5f,
                                -0.5f,0.5f

                        };
                        Buffer vertexBuffer = ByteBuffer.allocateDirect(vertexPoints.length * 4)
                                                        .order(ByteOrder.nativeOrder())
                                                        .asFloatBuffer()
                                                        .put(vertexPoints).position(0);

                        GLES20.glEnableVertexAttribArray(inputPointIndex);
                        GLES20.glVertexAttribPointer(inputPointIndex, 2, GLES20.GL_FLOAT, false, 0,
                                                     vertexBuffer);

                        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexPoints.length / 2);
                        GLES20.glDisableVertexAttribArray(inputPointIndex);

                        Buffer pixels = IntBuffer.allocate(surfaceWidth * surfaceHeight );
                        glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA,
                                     GLES20.GL_UNSIGNED_BYTE, pixels);
                        Bitmap bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight,
                                                            Bitmap.Config.ARGB_8888, true);
                        pixels.rewind();
                        bitmap.copyPixelsFromBuffer(pixels);
                        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
                        int eglGetError = EGL14.eglGetError();

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


    public static void 离屏渲染题图四边形() {


    }


    public static void 离屏渲染题图正方向居中裁剪() {


    }


}
