package com.nothing.commonutils.utils.mediacodec.egl;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.view.Surface;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.mediacodec.interfaces.OnFrameCallBack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class BaseEglRender {
    private static final String TAG = "BaseEglRender";
    protected boolean isDrawScreenShot;
    protected OnFrameCallBack mCallBack;
    protected EGLConfig mEglConfig;
    protected int mFps;
    protected int mHeight;
    protected boolean mIsRunning;
    protected int mVideoInterval;
    protected int mWidth;
    protected byte[] screenData;
    private int mCount = 1;
    private long mTime = 0;
    protected EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    protected EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    protected EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    protected final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    protected abstract void awaitNewImage();

    protected abstract void drawImage();

    protected abstract void releaseImage();

    public void setCallBack(OnFrameCallBack onFrameCallBack) {
        this.mCallBack = onFrameCallBack;
    }

    public void eglSetup(Surface surface) {
        EGLDisplay eglGetDisplay = EGL14.eglGetDisplay(0);
        this.mEGLDisplay = eglGetDisplay;
        if (eglGetDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] iArr = new int[2];
        if (!EGL14.eglInitialize(this.mEGLDisplay, iArr, 0, iArr, 1)) {
            this.mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }
        EGLConfig[] eGLConfigArr = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(this.mEGLDisplay, new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 8, 12352, 4, 12344}, 0, eGLConfigArr, 0, 1, new int[1], 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }
        EGLConfig eGLConfig = eGLConfigArr[0];
        this.mEglConfig = eGLConfig;
        this.mEGLContext = EGL14.eglCreateContext(this.mEGLDisplay, eGLConfig, EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
        checkEglError("eglCreateContext");
        if (this.mEGLContext == null) {
            throw new RuntimeException("null context");
        }
        this.mEGLSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, this.mEglConfig, surface, new int[]{12344}, 0);
        checkEglError("eglCreatePbufferSurface");
        if (this.mEGLSurface == null) {
            throw new RuntimeException("surface was null");
        }
    }

    protected void checkEglError(String str) {
        int eglGetError = EGL14.eglGetError();
        if (eglGetError == 12288) {
            return;
        }
        throw new RuntimeException(str + ": EGL error: 0x" + Integer.toHexString(eglGetError));
    }

    public void makeCurrent() {
        EGLDisplay eGLDisplay = this.mEGLDisplay;
        EGLSurface eGLSurface = this.mEGLSurface;
        if (!EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, this.mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    protected void setPresentationTime(long j) {
        EGLExt.eglPresentationTimeANDROID(this.mEGLDisplay, this.mEGLSurface, j);
        checkEglError("eglPresentationTimeANDROID");
    }

    public void initFPs(int i) {
        this.mFps = i;
        this.mVideoInterval = 1000 / i;
    }

    protected long computePresentationTimeNsec(int i) {
        return (i * 1000000000) / this.mFps;
    }

    public void stop() {
        Lg.i(TAG, "EGLRender stop");
        this.mIsRunning = false;
    }

    public boolean isRunning() {
        return this.mIsRunning;
    }

    public byte[] getScreenShot() {
        this.isDrawScreenShot = true;
        return this.screenData;
    }

    public void start() {
           Lg.i(TAG, "EGLRender run");
        this.mIsRunning = true;
        while (this.mIsRunning) {
            makeCurrent();
            awaitNewImage();
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - this.mTime >= this.mVideoInterval) {
                OnFrameCallBack onFrameCallBack = this.mCallBack;
                if (onFrameCallBack != null && onFrameCallBack.canUpdateFrame()) {
                    drawImage();
                    this.mCallBack.onUpdateFrame();
                    int i = this.mCount;
                    this.mCount = i + 1;
                    setPresentationTime(computePresentationTimeNsec(i));
                    swapBuffers();
                }
                this.mTime = currentTimeMillis;
            }
        }
    }

    protected boolean swapBuffers() {
        boolean eglSwapBuffers = EGL14.eglSwapBuffers(this.mEGLDisplay, this.mEGLSurface);
        checkEglError("eglSwapBuffers");
        return eglSwapBuffers;
    }

    public void drawScreenBitmap() {
        this.isDrawScreenShot = false;
        final ByteBuffer allocateDirect = ByteBuffer.allocateDirect(this.mWidth * this.mHeight * 4);
        allocateDirect.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, this.mWidth, this.mHeight, 6408, 5121, allocateDirect);
        allocateDirect.rewind();
        this.singleThreadExecutor.execute(new Runnable() {
            @Override
            public final void run() {
                BaseEglRender.this.render2Bitmap(allocateDirect);
            }
        });
    }


    public void render2Bitmap(ByteBuffer byteBuffer) {
        Matrix matrix = new Matrix();
        matrix.postScale(1.0f, -1.0f);
        Bitmap createBitmap = Bitmap.createBitmap(this.mWidth, this.mHeight, Bitmap.Config.ARGB_8888);
        createBitmap.copyPixelsFromBuffer(byteBuffer);
        this.screenData = bitmap2Bytes(Bitmap.createBitmap(createBitmap, 0, 0, createBitmap.getWidth(), createBitmap.getHeight(), matrix, true));
    }

    public byte[] bitmap2Bytes(Bitmap bitmap) {
        if (bitmap == null) {
            return new byte[0];
        }
        ByteBuffer allocate = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(allocate);
        return allocate.array();
    }

    protected Bitmap convertBitmap(int[] iArr) {
        int[] iArr2 = new int[iArr.length];
        int i = 0;
        while (true) {
            int i2 = this.mHeight;
            if (i < i2) {
                int i3 = this.mWidth;
                int i4 = i * i3;
                int i5 = ((i2 - i) - 1) * i3;
                for (int i6 = 0; i6 < this.mWidth; i6++) {
                    int i7 = iArr[i4 + i6];
                    iArr2[i5 + i6] = (i7 & (-16711936)) | ((i7 << 16) & 16711680) | ((i7 >> 16) & 255);
                }
                i++;
            } else {
                return Bitmap.createBitmap(iArr2, this.mWidth, i2, Bitmap.Config.ARGB_8888);
            }
        }
    }

    public void release() {
        EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
        EGL14.eglMakeCurrent(this.mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(this.mEGLDisplay);
    }
}
