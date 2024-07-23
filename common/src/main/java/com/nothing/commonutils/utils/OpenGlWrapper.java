package com.nothing.commonutils.utils;


import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.text.TextUtils;

public class OpenGlWrapper {


    public static boolean glLinkStatus(int program) {
        int[] params = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, params, 0);
        return params[0] == 1;
    }

    public static int glInfoLogLength(int program) {
        return glGetProgramiv(program, GLES20.GL_INFO_LOG_LENGTH)[0];
    }

    public static int glGetCurrentProgram() {
        return glGetIntegerv(GLES20.GL_CURRENT_PROGRAM)[0];
    }

    public static int[] glGetMAXViewportDIMS() {
        int[] led = glGetIntegerv(GLES20.GL_MAX_VIEWPORT_DIMS);
        return new int[]{led[0],led[1]};
    }
   public static int glGetMAXTextureSize() {
        int[] led = glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE);
        return  led[0];
    }

    public static int glMaxRenderbufferSize() {
        int[] led = glGetIntegerv(GLES20.GL_MAX_RENDERBUFFER_SIZE);
        return led[0];
    }


    //GL_TEXTURE0
    public static int glActiveTexture() {
        return glGetIntegerv(GLES20.GL_ACTIVE_TEXTURE)[0];
    }

    public static boolean glShaderCompiler() {
        return glGetBooleanv(GLES20.GL_SHADER_COMPILER)[0];
    }

    public static int[] glGetIntegerv(int pname) {
        int[] params = new int[16];
        GLES20.glGetIntegerv(pname, params, 0);
        return params;
    }

    public static float[] glGetUniformfv(int program,int location) {
        float[] params = new float[4];
        GLES20.glGetUniformfv(program,location, params, 0);
        return params;
    }


    public static float[] glGetFloatv(int pname) {
        float[] params = new float[16];
        GLES20.glGetFloatv(pname, params, 0);

        return params;
    }

    public static boolean[] glGetBooleanv(int pname) {
        boolean[] params = new boolean[16];
        GLES20.glGetBooleanv(pname, params, 0);
        return params;
    }

    public static int[] glGetProgramiv(int program, int pname) {
        int[] params = new int[16];
        GLES20.glGetProgramiv(program, pname, params, 0);
        return params;
    }

    public static int[] glGetShaderiv(int shader, int pname) {
        int[] params = new int[16];
        GLES20.glGetShaderiv(shader, pname, params, 0);
        return params;
    }


    public static int[] glGetAttachedShaders(int program, int maxcount) {
        int[] shaders = new int[maxcount];
        GLES20.glGetAttachedShaders(program, maxcount, new int[maxcount], 0, shaders, 0);
        return shaders;
    }

    public static String glGetGLError(int program, int maxcount) {
        StringBuilder stringBuilder = new StringBuilder();
        int logLength = glGetProgramiv(program, GLES20.GL_INFO_LOG_LENGTH)[0];
        if (logLength > 0) {
            String infoLog = GLES20.glGetProgramInfoLog(program);
            if (!TextUtils.isEmpty(infoLog)) {
                stringBuilder.append("Program:\n");
                stringBuilder.append(infoLog);
                stringBuilder.append("\n");
            }
        }
        for (int shader : glGetAttachedShaders(program, maxcount)) {
            if (shader <= 0) {
                continue;
            }
            if (glGetShaderiv(shader, GLES20.GL_SHADER_TYPE)[0] == GLES20.GL_VERTEX_SHADER) {
                String shaderInfoLog = GLES20.glGetShaderInfoLog(shader);
                if (!TextUtils.isEmpty(shaderInfoLog)) {
                    stringBuilder.append("VERTEX:\n");
                    stringBuilder.append(shaderInfoLog);
                    stringBuilder.append("\n");
                }
            } else if (glGetShaderiv(shader, GLES20.GL_SHADER_TYPE)[0] ==
                       GLES20.GL_FRAGMENT_SHADER) {
                String shaderInfoLog = GLES20.glGetShaderInfoLog(shader);
                if (!TextUtils.isEmpty(shaderInfoLog)) {
                    stringBuilder.append("FRAGMENT:\n");
                    stringBuilder.append(shaderInfoLog);
                    stringBuilder.append("\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    public static boolean initEGLContext(int surfaceWidth, int surfaceHeight) {
        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        boolean eglInitialize = EGL14.eglInitialize(eglDisplay, null, 0, null, 0);
        int[] choose_attrib_list
                = new int[]{EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE};
        EGLConfig[] eglConfigs = new EGLConfig[1];
        boolean eglChooseConfig = EGL14.eglChooseConfig(eglDisplay,
                                                        choose_attrib_list, 0,
                                                        eglConfigs, 0,
                                                        eglConfigs.length,
                                                        new int[eglConfigs.length],
                                                        0);
        EGLConfig eglConfig = eglConfigs[0];
        int[] suface_attrib_list
                = new int[]{EGL14.EGL_WIDTH, surfaceWidth, EGL14.EGL_HEIGHT, surfaceHeight, EGL14.EGL_NONE};
        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig,
                                                              suface_attrib_list,
                                                              0);
        int[] context_attrib_list
                = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        EGLContext eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig,
                                                       EGL14.EGL_NO_CONTEXT,
                                                       context_attrib_list, 0);
        boolean eglMakeCurrent = EGL14.eglMakeCurrent(eglDisplay, eglSurface,
                                                      eglSurface, eglContext);
        return eglMakeCurrent;
    }



}
