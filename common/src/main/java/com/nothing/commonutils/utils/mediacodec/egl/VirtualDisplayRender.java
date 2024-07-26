package com.nothing.commonutils.utils.mediacodec.egl;

import android.graphics.Rect;
import android.opengl.GLES20;

import com.nothing.commonutils.utils.mediacodec.filter.BlackScreenFilter;
import com.nothing.commonutils.utils.mediacodec.filter.MediaRecordFilter;

public class VirtualDisplayRender {
    private static final String TAG = "VirtualDisplayRender";
    private BlackScreenFilter blackScreenFilter;
    private MediaRecordFilter mediaRecordFilter;

    public void surfaceCreated() {
        BlackScreenFilter blackScreenFilter = new BlackScreenFilter();
        this.blackScreenFilter = blackScreenFilter;
        blackScreenFilter.initOpenGl();
        MediaRecordFilter mediaRecordFilter = new MediaRecordFilter();
        this.mediaRecordFilter = mediaRecordFilter;
        mediaRecordFilter.initOpenGl();
    }

    public void setBlackScreen(boolean z) {
        BlackScreenFilter blackScreenFilter = this.blackScreenFilter;
        if (blackScreenFilter != null) {
            blackScreenFilter.setBlackScreen(z);
        }
    }

    public void updateMatrix(int i, int i2) {
        this.blackScreenFilter.updateScreen(i, i2);
        this.mediaRecordFilter.updateScreen(i, i2);
        updateFBO(i, i2);
    }

    public void updateRect(Rect rect) {
        MediaRecordFilter mediaRecordFilter = this.mediaRecordFilter;
        if (mediaRecordFilter == null || rect == null) {
            return;
        }
        mediaRecordFilter.updateRect(rect);
    }

    public int getVirtualDisplayTextureId() {
        int[] iArr = new int[1];
        GLES20.glGenTextures(1, iArr, 0);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, iArr[0]);
        return iArr[0];
    }

    public int drawFrame(int i) {
        return this.mediaRecordFilter.onDrawTexture(this.blackScreenFilter.onDrawTexture(i));
    }

    private void updateFBO(int i, int i2) {
        BlackScreenFilter blackScreenFilter = this.blackScreenFilter;
        if (blackScreenFilter != null) {
            blackScreenFilter.updateFBO(i, i2);
        }
    }

    public void release() {
        BlackScreenFilter blackScreenFilter = this.blackScreenFilter;
        if (blackScreenFilter != null) {
            blackScreenFilter.release();
        }
        MediaRecordFilter mediaRecordFilter = this.mediaRecordFilter;
        if (mediaRecordFilter != null) {
            mediaRecordFilter.release();
        }
    }
}
