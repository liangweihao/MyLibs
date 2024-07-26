package com.nothing.commonutils.utils.mediacodec;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.mediacodec.bean.CodecParameter;
import com.nothing.commonutils.utils.mediacodec.entity.ScreenShotInfo;
import com.nothing.commonutils.utils.mediacodec.interfaces.ICaptureStatusListener;
import com.nothing.commonutils.utils.mediacodec.interfaces.IDisplayInfo;
import com.nothing.commonutils.utils.mediacodec.interfaces.OnScreenShareCallback;
import com.nothing.commonutils.utils.mediacodec.utils.MediaCodecUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseMediaEncoder {
    private static final String TAG = "BaseMediaEncoder";
    private final Handler mHandler;
    protected boolean acquireLatestImage;
    private MediaCodecUtils codecUtils;
    protected Rect cropRect;
    protected ICaptureStatusListener listener;
    private MediaProjection.Callback mCallback;
    protected CodecParameter mCodecParameter;
    protected VirtualDisplay mDesktopDisplay;
    protected long mDiscardFrames;
    protected IDisplayInfo mDisplayInfo;
    protected MediaProjection mMediaProjection;
    protected OnScreenShareCallback mOnScreenCallBack;
    protected Point mRecordSize;
    protected VirtualDisplay mScreenshotDisplay;
    protected long mTotalFrames;
    protected ManagerMediaProjection managerMediaProjection;
    protected int mRecordOrientation = -1;
    protected AtomicBoolean mIsRunning = new AtomicBoolean(false);
    protected AtomicBoolean mIsRotated = new AtomicBoolean(false);
    protected AtomicBoolean mReset = new AtomicBoolean(false);
    protected AtomicBoolean mForceQuit = new AtomicBoolean(false);
    protected AtomicBoolean mIsDesktop = new AtomicBoolean(false);
    protected AtomicBoolean mPauseDesktop = new AtomicBoolean(false);
    protected AtomicBoolean mPictureGot = new AtomicBoolean(false);

    protected abstract void createDesktopVirtualDisplay(int width, int height);

    protected abstract void createScreenshotVirtualDisplay(int i, int i2);

    public abstract byte[] getScreenShot();

    protected abstract boolean isUseEgl();

    public abstract void openBlackScreen(boolean z);

    public void postAcquireLatestImage() {
    }

    private ExecutorService executorService;
    private HandlerThread handlerThread;

    public BaseMediaEncoder() {

        handlerThread = new HandlerThread("AsyncHandler");
        handlerThread.start();
        this.mHandler   = new Handler(handlerThread.getLooper());
        executorService = Executors.newFixedThreadPool(3);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            executorService.shutdownNow();
            handlerThread.quit();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected abstract void recordVirtualDisplay(boolean z) throws RuntimeException;

    public void init(IDisplayInfo iDisplayInfo, ManagerMediaProjection managerMediaProjection) {
        this.mDisplayInfo           = iDisplayInfo;
        this.managerMediaProjection = managerMediaProjection;
    }

    public MediaCodecUtils getCodecUtils() {
        if (this.codecUtils == null) {
            this.codecUtils = new MediaCodecUtils(getCodecParameter());
        }
        return this.codecUtils;
    }

    public void setCaptureListener(ICaptureStatusListener iCaptureStatusListener) {
        this.listener = iCaptureStatusListener;
    }

    public void createVirtualDisplay(boolean isDesktop, int width, int height) {
        ManagerMediaProjection managerMediaProjection = this.managerMediaProjection;
        if (managerMediaProjection != null && managerMediaProjection.hasMediaProjection()) {
            MediaProjection mediaProjection = this.managerMediaProjection.getMediaProjection();
            this.mMediaProjection = mediaProjection;
            if (mediaProjection != null) {
                this.mRecordSize        = new Point(width, height);
                this.mRecordOrientation = getDisplayOrientation();
                MediaProjection.Callback callback = new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        super.onStop();
                        Lg.d(TAG, "MediaProjection callback stop");
                        BaseMediaEncoder.this.stopCapture();
                    }
                };
                this.mCallback = callback;
                this.mMediaProjection.registerCallback(callback, mHandler);
                if (isDesktop) {
                    onStartCallback();
                }
                if (isUseEgl()) {
                    createDesktopVirtualDisplay(width, height);
                    return;
                }
                if (isDesktop) {
                    createDesktopVirtualDisplay(width, height);
                }
                createScreenshotVirtualDisplay(width, height);
                return;
            }
        }
        ICaptureStatusListener iCaptureStatusListener = this.listener;
        if (iCaptureStatusListener != null) {
            iCaptureStatusListener.onCaptureStatusChanged(2,
                                                          ICaptureStatusListener.GET_MEDIA_PROJECTION_ERROR);
        }
    }

    public void startCapture(boolean isDesktop) {
        Lg.i(TAG, ">>>> [desktop][helper] startCapture" + isDesktop);
        doCapture(isDesktop);
        ICaptureStatusListener iCaptureStatusListener = this.listener;
        if (iCaptureStatusListener != null) {
            iCaptureStatusListener.onCaptureStatusChanged(0,
                                                          ICaptureStatusListener.PREPARE_START_CAPTURE_SCREEN);
        }
    }

    private void doCapture(boolean isDesktop) {
        executorService.execute(new Runnable() {
            @Override
            public final void run() {
                BaseMediaEncoder.this.starCaptureRecorderIfNotRunnning(isDesktop);
            }
        });
    }


    public void starCaptureRecorderIfNotRunnning(boolean isDesktop) {
        if (isRunning()) {
            return;
        }
        startCaptureRecorder(isDesktop);
    }

    private void startCaptureRecorder(boolean isDesktop) {
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("startCaptureRecorder ......");
        sb.append(isDesktop ? "desktop" : "screenshot");
        Lg.i(str, sb.toString());
        this.mIsRunning.set(true);
        this.mIsDesktop.set(isDesktop);
        CodecParameter codecParameter = getCodecParameter();
        int i = 0;
        do {
            String str2 = TAG;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("startCaptureRecorder");
            sb2.append(i == 0 ? "..." : ", retryCount");
            Lg.i(str2, sb2.toString());
            try {
                if (getCodecUtils().prepareEncode()) {
                    if (this.managerMediaProjection != null) {
                        createVirtualDisplay(isDesktop, codecParameter.getRecordWidth(),
                                             codecParameter.getRecordHeight());
                        recordVirtualDisplay(isDesktop);
                    } else {
                        Lg.e(TAG, "MediaProjection is null ");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                String str3 = TAG;
                Lg.i(str3, "startCaptureRecorder, exception: " + e.getLocalizedMessage());
                String str4 = TAG;
                Lg.e(str4, "retryCount = " + i);
                i++;
                if (i >= 3) {
                    ICaptureStatusListener iCaptureStatusListener = this.listener;
                    if (iCaptureStatusListener != null) {
                        iCaptureStatusListener.onCaptureStatusChanged(4,
                                                                      ICaptureStatusListener.FAIL_TO_CAPTURE_SCREEN);
                    }
                    release();
                }
            }
            if (i >= 3) {
                return;
            }
        } while (!this.mForceQuit.get());
    }

    public void stopCapture(boolean forceStop) {
        this.acquireLatestImage = false;
        if (forceStop) {
            stopCapture();
        } else if (isDesktop()) {
        } else {
            stopCapture();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void pauseDesktop() {
        Lg.i(TAG, "[desktop] pauseCapture");
        this.mPauseDesktop.set(true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void resumeDesktop() {
        Lg.i(TAG, "[desktop] resumeCapture");
        this.mPauseDesktop.set(false);
        this.mPictureGot.set(false);
    }

    public void rotateAndResetCapture(boolean z) {
        stopCapture(z);
        setRotated(true);
        if (this.mCodecParameter == null) {
            this.mCodecParameter = initCodecParameter();
        }
        startCapture(z);
    }

    public void resetCapture(boolean z) {
        stopCapture(z);
        if (this.mCodecParameter == null) {
            this.mCodecParameter = initCodecParameter();
        }
        startCapture(z);
    }

    public void resetCapture(boolean z, CodecParameter codecParameter) {
        stopCapture(z);
        this.mCodecParameter = codecParameter;
        startCapture(z);
    }

    public CodecParameter getCurrentCodecParameter() {
        return this.mCodecParameter;
    }

    public Point getRecordSize() {
        Point point = this.mRecordSize;
        return point == null ? getDisplaySize() : point;
    }

    public int getRecordOrientation() {
        int i = this.mRecordOrientation;
        return i == -1 ? getDisplayOrientation() : i;
    }

    public Point getDisplaySize() {
        IDisplayInfo iDisplayInfo = this.mDisplayInfo;
        return iDisplayInfo != null ? iDisplayInfo.getDisplaySize() : new Point(360, 720);
    }

    public Point getScreenSize() {
        IDisplayInfo iDisplayInfo = this.mDisplayInfo;
        return iDisplayInfo != null ? iDisplayInfo.getScreenSize() : new Point(1080, 1920);
    }

    public void setCropRegionRect(Rect rect) {
        this.cropRect = rect;
        CodecParameter codecParameter = this.mCodecParameter;
        if (codecParameter != null) {
            codecParameter.setCropRegion(rect);
        }
    }

    public Rect getCropRegionRect() {
        return this.cropRect;
    }

    public int getDisplayOrientation() {
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("getDisplayOrientation>>>>>>");
        IDisplayInfo iDisplayInfo = this.mDisplayInfo;
        sb.append(iDisplayInfo == null ? "mDisplayInfo  isEmpty" : Integer.valueOf(
                iDisplayInfo.getOrientation()));
        Lg.i(str, sb.toString());
        IDisplayInfo iDisplayInfo2 = this.mDisplayInfo;
        if (iDisplayInfo2 != null) {
            return iDisplayInfo2.getOrientation();
        }
        return 0;
    }

    public byte[] getParams() {
        Point recordSize = getRecordSize();
        return new ScreenShotInfo(recordSize.x, recordSize.y, 1).getData();
    }

    public boolean isRunning() {
        return this.mIsRunning.get();
    }

    public boolean isRecording() {
        return this.mIsRunning.get() && !this.mForceQuit.get();
    }

    public boolean isDesktop() {
        return this.mIsDesktop.get();
    }

    public boolean isRotated() {
        return (this.mRecordOrientation == -1 ||
                getDisplayOrientation() == this.mRecordOrientation) ? false : true;
    }

    public void setRotated(boolean z) {
        this.mIsRotated.set(z);
    }

    public boolean getReseted() {
        return this.mReset.get();
    }

    public void setReseted(boolean z) {
        this.mReset.set(z);
    }

    public long getTotalFrames() {
        return this.mTotalFrames;
    }

    public long getDiscardFrames() {
        return this.mDiscardFrames;
    }

    public void setOnScreenShareCallBack(OnScreenShareCallback onScreenShareCallback) {
        this.mOnScreenCallBack = onScreenShareCallback;
    }

    protected void onStartCallback() {
        Lg.i(TAG, "MediaProjection start record.");
    }

    protected void onStopCallback() {
        Lg.i(TAG, "MediaProjection end record.");
    }

    private CodecParameter getCodecParameter() {
        if (this.mCodecParameter == null) {
            this.mCodecParameter = initCodecParameter();
        }
        boolean checkOrientation = checkOrientation(this.mCodecParameter.getRecordWidth(),
                                                    this.mCodecParameter.getRecordHeight());
        CodecParameter codecParameter = this.mCodecParameter;
        int recordWidth
                = checkOrientation ? codecParameter.getRecordWidth() : codecParameter.getRecordHeight();
        CodecParameter codecParameter2 = this.mCodecParameter;
        int recordHeight
                = checkOrientation ? codecParameter2.getRecordHeight() : codecParameter2.getRecordWidth();
        CodecParameter codecParameter3 = this.mCodecParameter;
        int displayWidth
                = checkOrientation ? codecParameter3.getDisplayWidth() : codecParameter3.getDisplayHeight();
        int displayHeight
                = checkOrientation ? this.mCodecParameter.getDisplayHeight() : this.mCodecParameter.getDisplayWidth();
        this.mCodecParameter.setRecordWidth(recordWidth);
        this.mCodecParameter.setRecordHeight(recordHeight);
        this.mCodecParameter.setDisplayWidth(displayWidth);
        this.mCodecParameter.setDisplayHeight(displayHeight);
        return this.mCodecParameter;
    }

    private CodecParameter initCodecParameter() {
        Point displaySize = getDisplaySize();
        Point screenSize = getScreenSize();
        CodecParameter codecParameter = new CodecParameter();
        codecParameter.setBitrate((int) (displaySize.x * 3.5d * displaySize.y));
        codecParameter.setFrameRate(30);
        codecParameter.setBitrateMode(2);
        codecParameter.setRecordWidth(displaySize.x);
        codecParameter.setRecordHeight(displaySize.y);
        codecParameter.setDisplayWidth(screenSize.x);
        codecParameter.setDisplayHeight(screenSize.y);
        Rect cropRegionRect = getCropRegionRect();
        if (cropRegionRect != null) {
            codecParameter.setCropRegion(cropRegionRect);
        }
        return codecParameter;
    }

    protected boolean checkOrientation(int i, int i2) {
        Point displaySize = getDisplaySize();
        return (displaySize.x > displaySize.y && i > i2) ||
               (displaySize.x < displaySize.y && i < i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopCapture() {
        stopCaptureRecorder();
    }

    private void stopCaptureRecorder() {
        this.mForceQuit.set(true);
        if (this.mIsRunning.get()) {
            this.mIsRunning.set(false);
            Lg.i(TAG, "stopCaptureRecorder start.");
            release();
            Lg.i(TAG, "stopCaptureRecorder end.");
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void release() {
        onStopCallback();
        Lg.i(TAG, "screen capture release.");
        try {
            ManagerMediaProjection managerMediaProjection = this.managerMediaProjection;
            if (managerMediaProjection != null) {
                managerMediaProjection.releaseMediaProjection(this.mCallback);
            }
            OnScreenShareCallback onScreenShareCallback = this.mOnScreenCallBack;
            if (onScreenShareCallback != null) {
                onScreenShareCallback.onClearNeeded(0, 0);
            }
            VirtualDisplay virtualDisplay = this.mDesktopDisplay;
            if (virtualDisplay != null) {
                virtualDisplay.release();
                this.mDesktopDisplay = null;
            }
            VirtualDisplay virtualDisplay2 = this.mScreenshotDisplay;
            if (virtualDisplay2 != null) {
                virtualDisplay2.release();
                this.mScreenshotDisplay = null;
            }
            MediaCodecUtils mediaCodecUtils = this.codecUtils;
            if (mediaCodecUtils != null) {
                mediaCodecUtils.release();
            }
        } catch (Exception e) {
            String str = TAG;
            Lg.i(str, "screen capture release. exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
