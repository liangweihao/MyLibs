package com.nothing.commonutils.utils.mediacodec;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.mediacodec.bean.CodecParameter;
import com.nothing.commonutils.utils.mediacodec.interfaces.ICaptureStatusListener;
import com.nothing.commonutils.utils.mediacodec.interfaces.IDisplayInfo;
import com.nothing.commonutils.utils.mediacodec.interfaces.OnScreenShareCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ScreenCapture implements OnScreenShareCallback {
    public static List<byte[]> bufferList = new ArrayList();
    private static volatile ScreenCapture instance;
    private IDisplayInfo displayInfo;
    private boolean getTopFrame;
    private final BaseMediaEncoder mEncoder;

    private static final String TAG = "ScreenCapture";


    private void startCheckKeepAlive() {
    }

    private void stopCheckKeepAlive() {
    }

    @Override // com.nothing.commonutils.utils.mediacodec.interfaces.OnScreenShareCallback
    public boolean onGetWriteState() {
        return false;
    }

    public static ScreenCapture getInstance() {
        if (instance == null) {
            synchronized (ScreenCapture.class) {
                if (instance == null) {
                    instance = new ScreenCapture();
                }
            }
        }
        return instance;
    }

    private ScreenCapture() {
        if (bufferList == null) {
            bufferList = new ArrayList();
        }
        bufferList    = Collections.synchronizedList(bufferList);
        this.mEncoder = new MediaEncoderWithEgl();
    }

    public void init(IDisplayInfo iDisplayInfo) {
        this.displayInfo = iDisplayInfo;
    }

    public void createVirtualDisplay(boolean isDesktop, int width, int height) {
        this.mEncoder.createVirtualDisplay(isDesktop, width, height);
    }

    public void startCapture(boolean isDesktop) {
        this.mEncoder.startCapture(isDesktop);
    }

    public void stopCapture(boolean forceStop) {
        if (!isDesktop() || forceStop) {
            stopCaptureForce(forceStop);
        }
    }

    public CodecParameter getCurrentCodecParameter() {
        BaseMediaEncoder baseMediaEncoder = this.mEncoder;
        if (baseMediaEncoder != null) {
            return baseMediaEncoder.getCurrentCodecParameter();
        }
        return null;
    }

    public Rect getCropRegionRect() {
        BaseMediaEncoder baseMediaEncoder = this.mEncoder;
        if (baseMediaEncoder != null) {
            return baseMediaEncoder.getCropRegionRect();
        }
        return null;
    }

    public void stopCaptureForce(boolean forceStop) {
        this.mEncoder.stopCapture(forceStop);
        List<byte[]> list = bufferList;
        if (list != null) {
            list.clear();
        }
    }

    
    public void resetCapture(boolean z) {
        this.mEncoder.resetCapture(z);
    }

    
    public void resetCapture(boolean z, CodecParameter codecParameter) {
        this.mEncoder.resetCapture(z, codecParameter);
    }

    public void openBlackScreen(boolean z) {
        this.mEncoder.openBlackScreen(z);
        if (z) {
            startCheckKeepAlive();
        } else {
            stopCheckKeepAlive();
        }
    }

    public byte[] getTopFrame() {
        try {
            this.getTopFrame = true;
            this.mEncoder.postAcquireLatestImage();
            List<byte[]> list = bufferList;
            if (list != null && list.size() > 0) {
                return bufferList.remove(0);
            }
        } catch (Exception e) {
            Log.i(TAG, "[MediaProjection] getTopFrame exception: " + e.getLocalizedMessage());
        }
        return new byte[0];
    }

    public byte[] getScreenShot() {
        return this.mEncoder.getScreenShot();
    }

    public boolean isRunning() {
        return this.mEncoder.isRunning();
    }

    public boolean isRecording() {
        return this.mEncoder.isRecording();
    }

    public boolean isDesktop() {
        return this.mEncoder.isDesktop();
    }

    
    public boolean isRotated() {
        return this.mEncoder.isRotated();
    }

    
    public int getRecordOrientation() {
        return this.mEncoder.getRecordOrientation();
    }

    public int getDisplayOrientation() {
        return this.mEncoder.getDisplayOrientation();
    }

    public byte[] getParams() {
        return this.mEncoder.getParams();
    }

    public Point getDisplaySize() {
        return this.mEncoder.getDisplaySize();
    }

    
    public Point getRecordSize() {
        return this.mEncoder.getRecordSize();
    }

    
    public boolean getReseted() {
        return this.mEncoder.getReseted();
    }

    
    public void rotateAndResetCapture(boolean z) {
        this.mEncoder.rotateAndResetCapture(z);
    }

    
    public long getTotalFrames() {
        return this.mEncoder.getTotalFrames();
    }

    
    public long getDiscardFrames() {
        return this.mEncoder.getDiscardFrames();
    }

    public void pauseDesktop() {
        this.mEncoder.pauseDesktop();
    }

    public void resumeDesktop() {
        this.mEncoder.resumeDesktop();
    }

    
    public void setRotated(boolean z) {
        this.mEncoder.setRotated(z);
    }

    
    public void setReseted(boolean z) {
        this.mEncoder.setReseted(z);
    }

    public void initEncoder(
            IDisplayInfo iDisplayInfo, ManagerMediaProjection managerMediaProjection
    ) {
        this.mEncoder.init(iDisplayInfo, managerMediaProjection);
        this.mEncoder.setOnScreenShareCallBack(this);
    }

    public void setCropRegionRect(Rect rect) {
        BaseMediaEncoder baseMediaEncoder = this.mEncoder;
        if (baseMediaEncoder != null) {
            baseMediaEncoder.setCropRegionRect(rect);
        }
    }

    public void setCaptureListener(ICaptureStatusListener iCaptureStatusListener) {
        this.mEncoder.setCaptureListener(iCaptureStatusListener);
    }

    @Override 
    public void onH264Info(byte[] bArr, boolean isKeyFrame) {
        bufferList.add(bArr);
    }

    @Override  
    public long onClearNeeded(int totalFrameCount, int keyFrameCount) {
        BaseMediaEncoder baseMediaEncoder;
        if ((totalFrameCount > 0 || keyFrameCount > 0) && (baseMediaEncoder = this.mEncoder) != null &&
            baseMediaEncoder.listener != null) {
            this.mEncoder.listener.onCaptureStatusChanged(6, String.format(
                    "{\"total\":%d,\"keyframe\":%d\"}", Integer.valueOf(totalFrameCount), Integer.valueOf(
                            keyFrameCount)));
        }
        long size = bufferList.size();
        if (size > 0) {
            bufferList.clear();
            if (size > 25) {
                Lg.i(TAG, "****** remove h264 data, count: " + size);
            }
        }
        return size;
    }
}
