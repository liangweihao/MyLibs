package com.nothing.commonutils.utils.mediacodec.egl;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import com.nothing.commonutils.utils.Lg;

import androidx.annotation.Nullable;


public class EGLEnv extends BaseEglRender implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "EGLEnv";
    private boolean mFrameAvailable = true;
    private Surface mSurfaceEncoder;
    private SurfaceTexture mSurfaceTexture;
    private int mTextureId;
    private VirtualDisplayRender mTextureRender;

    public EGLEnv(Surface surface, int width, int height, int fps,@Nullable Rect rect) {
        this.mWidth = width;
        this.mHeight = height;
        initFPs(fps);
        eglSetup(surface);
        makeCurrent();
        setup(rect);
    }

    public void setBlackScreen(boolean z) {
        VirtualDisplayRender virtualDisplayRender = this.mTextureRender;
        if (virtualDisplayRender != null) {
            virtualDisplayRender.setBlackScreen(z);
        }
    }

    private void setup(Rect rect) {
        VirtualDisplayRender virtualDisplayRender = new VirtualDisplayRender();
        this.mTextureRender = virtualDisplayRender;
        virtualDisplayRender.surfaceCreated();
        this.mTextureRender.updateMatrix(this.mWidth, this.mHeight);
        this.mTextureRender.updateRect(rect);
        this.mTextureId = this.mTextureRender.getVirtualDisplayTextureId();
        String str = TAG;
        Log.e(str, "textureID=" + this.mTextureId);
        SurfaceTexture surfaceTexture = new SurfaceTexture(this.mTextureId);
        this.mSurfaceTexture = surfaceTexture;
        surfaceTexture.setDefaultBufferSize(this.mWidth, this.mHeight);
        this.mSurfaceTexture.setOnFrameAvailableListener(this);
        this.mSurfaceEncoder = new Surface(this.mSurfaceTexture);
    }

    public Surface getSurface() {
        return this.mSurfaceEncoder;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.mFrameAvailable = true;
    }

    @Override
    protected void awaitNewImage() {
        if (this.mFrameAvailable) {
            this.mFrameAvailable = false;
            this.mSurfaceTexture.updateTexImage();
        }
    }

    @Override
    protected void drawImage() {
        this.mTextureRender.drawFrame(this.mTextureId);
        if (this.isDrawScreenShot) {
            drawScreenBitmap();
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            releaseImage();
            VirtualDisplayRender virtualDisplayRender = this.mTextureRender;
            if (virtualDisplayRender != null) {
                virtualDisplayRender.release();
            }
            release();
        } catch (Exception e) {
            Lg.e(TAG, "release fail" + e.getMessage());
        }
    }

    @Override
    protected void releaseImage() {
        SurfaceTexture surfaceTexture = this.mSurfaceTexture;
        if (surfaceTexture != null) {
            surfaceTexture.releaseTexImage();
        }
    }
}
