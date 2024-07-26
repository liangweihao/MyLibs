package com.nothing.commonutils.utils.mediacodec;


import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

import com.nothing.commonutils.utils.Lg;


public class ManagerMediaProjection {
    private static final String TAG = "ManagerMediaProjection";
    private boolean destroyMediaProjection;
    private final Intent intent;
    private MediaProjection mediaProjection;
    private final MediaProjectionManager mediaProjectionManager;
    private boolean releaseMediaProjection;

    public ManagerMediaProjection(Intent intent, MediaProjectionManager mediaProjectionManager) {
        this.mediaProjectionManager = mediaProjectionManager;
        this.intent                 = intent;
    }

    public MediaProjection getMediaProjection() {
        if (this.mediaProjection == null) {
            this.mediaProjection = this.mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, this.intent);
        }
        return this.mediaProjection;
    }

    public boolean hasMediaProjection() {
        return (this.intent == null || this.mediaProjectionManager == null ||
                this.destroyMediaProjection ||
                (this.releaseMediaProjection && Build.VERSION.SDK_INT >= 34)) ? false : true;
    }

    public void destroyMediaProjection() {
        this.destroyMediaProjection = true;
        releaseMediaProjection(null);
    }

    public void releaseMediaProjection(MediaProjection.Callback callback) {
        try {
            MediaProjection mediaProjection = this.mediaProjection;
            if (mediaProjection != null) {
                if (callback != null) {
                    mediaProjection.unregisterCallback(callback);
                }
                this.mediaProjection.stop();
                this.mediaProjection = null;
            }
            this.releaseMediaProjection = true;
        } catch (Exception e) {
            Lg.i(TAG, "screen capture release. exception: " + e.getLocalizedMessage());
        }
    }
}