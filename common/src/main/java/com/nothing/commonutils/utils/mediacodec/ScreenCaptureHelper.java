package com.nothing.commonutils.utils.mediacodec;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.RootTools;
import com.nothing.commonutils.utils.mediacodec.bean.CodecParameter;
import com.nothing.commonutils.utils.mediacodec.entity.DisplayInfo;
import com.nothing.commonutils.utils.mediacodec.entity.MonitorInfo;
import com.nothing.commonutils.utils.mediacodec.interfaces.IDisplayInfo;

import java.util.concurrent.atomic.AtomicInteger;

public class ScreenCaptureHelper {
    private static final String TAG = "ScreenCaptureHelper";
    private ScreenCapture mCapture;
    private IDisplayInfo mDisplayInfo;

    public ScreenCaptureHelper(IDisplayInfo iDisplayInfo) {
        this.mDisplayInfo = iDisplayInfo;
        if (hasLollipop()) {
            ScreenCapture screenCapture = ScreenCapture.getInstance();
            this.mCapture = screenCapture;
            screenCapture.init(iDisplayInfo);
        }
    }

    public int startCapture() {
        AtomicInteger atomicInteger = new AtomicInteger();
        Lg.i(TAG, "[desktop][helper] startCapture");
        atomicInteger.set(startCaptureImpl());
        return atomicInteger.get();
    }

    private int startCaptureImpl() {
        if (hasLollipop()) {
            Lg.i(TAG, "[desktop][helper] startCaptureImpl");
            try {
                if (this.mCapture.isRunning()) {
                    this.mCapture.stopCapture(true);
                }
                this.mCapture.setRotated(false);
                this.mCapture.startCapture(true);
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    private boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= 21;
    }

    public int stopCapture() {
        Lg.i(TAG, "[desktop][helper] stopCapture");
        if (hasLollipop()) {
            try {
                this.mCapture.stopCapture(true);
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    public Rect getSourceRect() {
        CodecParameter currentCodecParameter = this.mCapture.getCurrentCodecParameter();
        if (currentCodecParameter != null) {
            return new Rect(0, 0, currentCodecParameter.getDisplayWidth(), currentCodecParameter.getDisplayHeight());
        }
        if (this.mDisplayInfo != null) {
            return new Rect(0, 0, this.mDisplayInfo.getScreenSize().x, this.mDisplayInfo.getScreenSize().y);
        }
        return null;
    }

    public Rect getCropRegionRect() {
        CodecParameter currentCodecParameter = this.mCapture.getCurrentCodecParameter();
        if (currentCodecParameter != null) {
            if (currentCodecParameter.getCropRegion() != null) {
                return currentCodecParameter.getCropRegion();
            }
            return null;
        }
        return this.mCapture.getCropRegionRect();
    }

    public int rotateCommand() {

        if (!hasLollipop() || Build.VERSION.SDK_INT >= 34) {
            return 0;
        }
        int orientation = getOrientation();
        int recordOrientation = this.mCapture.getRecordOrientation();
        String str = TAG;
        Lg.i(str, "[desktop][helper] rotateCommand( " + recordOrientation + " --> " + orientation + " ) ......");
        if (recordOrientation != orientation) {
            try {
                this.mCapture.rotateAndResetCapture(true);
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    public void resetCapture(CodecParameter codecParameter) {
        if (hasLollipop() && this.mCapture.isRunning()) {
            Lg.i(TAG, "[desktop][helper] resetCapture");
            try {
                this.mCapture.resetCapture(true, codecParameter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resetCapture(IDisplayInfo iDisplayInfo, CodecParameter codecParameter) {
        if (hasLollipop() && this.mCapture.isRunning()) {
            Lg.i(TAG, "[desktop][helper] resetCapture");
            try {
                this.mCapture.init(iDisplayInfo);
                this.mCapture.resetCapture(true, codecParameter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resetCapture() {
        if (hasLollipop() && this.mCapture.isRunning()) {
            Lg.i(TAG, "[desktop][helper] resetCapture");
            try {
                this.mCapture.resetCapture(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int enumScreen() {
        Lg.i(TAG, "[desktop][helper] enumScreen ......");
        return 1;
    }

    public byte[] getDisplayParams() {
        Point screenSize = this.mDisplayInfo.getScreenSize();
        return new DisplayInfo(screenSize.x, screenSize.y, this.mDisplayInfo.getOrientation()).getData();
    }

    public byte[] getParams() {
        int i;
        int i2;
        if (hasLollipop()) {
            Point recordSize = this.mCapture.getRecordSize();
            CodecParameter currentCodecParameter = this.mCapture.getCurrentCodecParameter();
            if (currentCodecParameter != null) {
                i2 = currentCodecParameter.getMediaCodecWidth();
                i = currentCodecParameter.getMediaCodecHeight();
            } else {
                int i3 = recordSize.x;
                i = recordSize.y;
                i2 = i3;
            }
            return new MonitorInfo(1, "screen1", 0, 0, i2, i, this.mCapture.getRecordOrientation()).getData();
        }
        return new MonitorInfo(1, "screen1", 0, 0, 720, 1280, 0).getData();
    }

    public int selectScreen(int i) {
        Lg.i(TAG, "[desktop][java] selectScreen ......");
        return 0;
    }

    public byte[] getFrame() {
        if (hasLollipop()) {
            if (this.mCapture.getReseted()) {
                this.mCapture.setReseted(false);
                Lg.i(TAG, "[desktop][helper] getFrame , size = 2");
                return new byte[]{48, 48};
            }
            byte[] topFrame = this.mCapture.getTopFrame();
            return topFrame.length == 0 ? new byte[]{48} : topFrame;
        }
        return new byte[]{48};
    }

    public long getTotalFrames() {
        if (hasLollipop()) {
            return this.mCapture.getTotalFrames();
        }
        return 0L;
    }

    public long getDiscardFrames() {
        if (hasLollipop()) {
            return this.mCapture.getDiscardFrames();
        }
        return 0L;
    }

    private int getOrientation() {
        IDisplayInfo iDisplayInfo = this.mDisplayInfo;
        if (iDisplayInfo != null) {
            return iDisplayInfo.getOrientation();
        }
        return 0;
    }

    private boolean isRoot() {
        return RootTools.isRootAvailable();
    }

    public boolean isRotated() {
        return hasLollipop() && this.mCapture.isRotated();
    }
}
