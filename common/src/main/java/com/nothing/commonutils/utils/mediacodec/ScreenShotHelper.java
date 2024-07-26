package com.nothing.commonutils.utils.mediacodec;


import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.mediacodec.interfaces.IDisplayInfo;

public class ScreenShotHelper {
    private boolean cancelByUser;
    private ScreenCapture mCapture;

    private static final String TAG = "ScreenShotHelper";

    public ScreenShotHelper(IDisplayInfo iDisplayInfo) {
        ScreenCapture screenCapture = ScreenCapture.getInstance();
        this.mCapture = screenCapture;
        screenCapture.init(iDisplayInfo);
    }

    private int startCaptureImpl() {
        Lg.i(TAG, "[screenshot][helper] startCaptureImpl");
        try {
            if (!this.mCapture.isRunning()) {
                this.mCapture.startCapture(false);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int startCapture() {
        Lg.i(TAG, "[screenshot][helper] startCapture");
        return startCaptureImpl();
    }

    public int stopCapture() {
        Lg.i(TAG, "[screenshot][helper] stopCapture");
        try {
            this.mCapture.stopCapture(false);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public byte[] getParams() {
        return this.mCapture.getParams();
    }

    public byte[] getScreenShot() {
        if (!this.mCapture.isRunning()) {
            startCapture();
            return new byte[0];
        }
        return this.mCapture.getScreenShot();
    }
}
