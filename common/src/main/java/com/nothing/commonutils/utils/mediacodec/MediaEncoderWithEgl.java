package com.nothing.commonutils.utils.mediacodec;

import android.hardware.display.DisplayManager;
import android.os.Build;
import com.nothing.commonutils.utils.mediacodec.egl.EGLEnv;
import com.nothing.commonutils.utils.mediacodec.interfaces.OnFrameCallBack;
import com.nothing.commonutils.utils.mediacodec.interfaces.OnScreenShareCallback;
import com.nothing.commonutils.utils.mediacodec.utils.BitrateController;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaEncoderWithEgl extends BaseMediaEncoder {
    private int drainCount;
    private int dropCount;
    private boolean isOpenBlackScreen;
    private BitrateController mController;
    private DisplayManager mDisplayManager;
    private int mDrainCount;
    private int mDropCount;
    private EGLEnv mEglRender;
    private final AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private long mLastTime;
    private OnFrameCallBack mOnFrameCallBack;
    private int mScreenDpi;

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    protected void createScreenshotVirtualDisplay(int i, int i2) {
    }

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    protected boolean isUseEgl() {
        return true;
    }

    public MediaEncoderWithEgl() {
        init();
    }

    private void init() {
        this.mController = new BitrateController();
        this.mScreenDpi = 1;
        this.mLastTime = 0L;
        this.dropCount = 0;
        this.drainCount = 0;
        this.mDropCount = 0;
        this.mDrainCount = 0;
        this.mOnFrameCallBack = new RenderFrameCallBack();
    }

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    protected void createDesktopVirtualDisplay(int i, int i2) {
        EGLEnv eGLEnv = this.mEglRender;
        if (eGLEnv != null) {
            eGLEnv.stop();
            this.mEglRender = null;
        }
        EGLEnv eGLEnv2 = new EGLEnv(getCodecUtils().getSurface(), i, i2, this.mCodecParameter.getFrameRate(), this.mCodecParameter.getScaleCropRegion());
        this.mEglRender = eGLEnv2;
        eGLEnv2.setBlackScreen(this.isOpenBlackScreen);
        this.mEglRender.setCallBack(this.mOnFrameCallBack);
        if (this.mMediaProjection != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                this.mDesktopDisplay = this.mMediaProjection.createVirtualDisplay("screen", i, i2, this.mScreenDpi, 16, this.mEglRender.getSurface(), null, null);
                return;
            }
            return;
        }
        DisplayManager displayManager = this.mDisplayManager;
        if (displayManager != null) {
            this.mDesktopDisplay = displayManager.createVirtualDisplay("screen", i, i2, this.mScreenDpi, this.mEglRender.getSurface(), 16);
        }
    }

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    protected void recordVirtualDisplay(boolean z) throws RuntimeException {
        this.mIsRunning.set(true);
        this.mEglRender.start();
        this.mIsRunning.set(false);
    }

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    public byte[] getScreenShot() {
        EGLEnv eGLEnv = this.mEglRender;
        return eGLEnv != null ? eGLEnv.getScreenShot() != null ? this.mEglRender.getScreenShot() : new byte[0] : new byte[0];
    }

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    public void openBlackScreen(boolean z) {
        this.isOpenBlackScreen = z;
        EGLEnv eGLEnv = this.mEglRender;
        if (eGLEnv != null) {
            eGLEnv.setBlackScreen(z);
        }
    }

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    public void stopCapture(boolean z) {
        EGLEnv eGLEnv = this.mEglRender;
        if (eGLEnv != null) {
            eGLEnv.stop();
        }
        super.stopCapture(z);
    }

    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    public void setOnScreenShareCallBack(OnScreenShareCallback onScreenShareCallback) {
        this.mOnScreenCallBack = onScreenShareCallback;
        getCodecUtils().setOnScreenShareCallBack(this.mOnScreenCallBack);
    }

    /* JADX INFO: Access modifiers changed from: private */
    
    public class RenderFrameCallBack implements OnFrameCallBack {
        private RenderFrameCallBack() {
        }

        @Override // com.nothing.commonutils.utils.mediacodec.interfaces.OnFrameCallBack
        public boolean canUpdateFrame() {
            if (MediaEncoderWithEgl.this.mOnScreenCallBack != null && !MediaEncoderWithEgl.this.mOnScreenCallBack.onGetWriteState()) {
                MediaEncoderWithEgl.this.calcFrameCount(false);
                return true;
            }
            MediaEncoderWithEgl.this.calcFrameCount(true);
            return false;
        }

        @Override // com.nothing.commonutils.utils.mediacodec.interfaces.OnFrameCallBack
        public void onUpdateFrame() {
            MediaEncoderWithEgl.this.getCodecUtils().encodeFrame();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void calcFrameCount(boolean z) {
        if (this.mLastTime == 0) {
            this.mLastTime = System.currentTimeMillis();
        }
        if (z) {
            this.dropCount++;
        } else {
            this.drainCount++;
        }
        if (System.currentTimeMillis() - this.mLastTime > 1000) {
            this.mLastTime = System.currentTimeMillis();
            this.mDrainCount = this.drainCount;
            this.mDropCount = this.dropCount;
            getCodecUtils().calcBitrate(this.mController.calcBitrate(this.mCodecParameter.getBitrate(), this.mCodecParameter.getFrameRate(), this.mDrainCount, this.mDropCount));
            this.drainCount = 0;
            this.dropCount = 0;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.nothing.commonutils.utils.mediacodec.BaseMediaEncoder
    public void release() {
        super.release();
        EGLEnv eGLEnv = this.mEglRender;
        if (eGLEnv != null) {
            eGLEnv.stop();
            this.mEglRender = null;
        }
    }
}
