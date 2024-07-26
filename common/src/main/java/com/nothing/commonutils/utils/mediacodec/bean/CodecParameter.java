package com.nothing.commonutils.utils.mediacodec.bean;

import android.graphics.Rect;
public class CodecParameter {
    public static final int BITRATE_MODE_CBR = 2;
    public static final int BITRATE_MODE_CQ = 0;
    public static final int BITRATE_MODE_VBR = 1;
    public static final int DEFAULT_BITRATE = 2000000;
    public static final int DEFAULT_FRAME_RATE = 30;
    public static final int DEFAULT_I_FRAME_INTERVAL = 1;
    private int bitrate;
    private int bitrateMode;
    private Rect cropRegionRect;
    private int displayHeight;
    private int displayWidth;
    private int frameRate;
    private int iframeInterval;
    private int recordHeight;
    private int recordWidth;

    public CodecParameter() {
        this.bitrate = DEFAULT_BITRATE;
        this.bitrateMode = 2;
        this.frameRate = 30;
        this.iframeInterval = 1;
        this.recordWidth = 720;
        this.recordHeight = 1280;
    }

    public CodecParameter(int i, int i2, int i3, int i4, int i5, int i6) {
        this.bitrate = i;
        this.bitrateMode = i2;
        this.frameRate = i3;
        this.iframeInterval = i6;
        this.recordWidth = i4;
        this.recordHeight = i5;
    }

    public void setCropRegion(Rect rect) {
        this.cropRegionRect = rect;
    }

    public void setCropRegion(int i, int i2, int i3, int i4) {
        this.cropRegionRect = new Rect(i, i2, i3, i4);
    }

    public Rect getCropRegion() {
        return this.cropRegionRect;
    }

    public Rect getScaleCropRegion() {
        if (this.cropRegionRect != null) {
            float cropRegionScale = getCropRegionScale();
            return new Rect((int) (this.cropRegionRect.left * cropRegionScale), (int) (this.cropRegionRect.top * cropRegionScale), (int) (this.cropRegionRect.right * cropRegionScale), (int) (this.cropRegionRect.bottom * cropRegionScale));
        }
        return null;
    }

    public int getMediaCodecWidth() {
        Rect rect = this.cropRegionRect;
        if (rect != null) {
            return (((int) (rect.width() * getCropRegionScale())) / 8) * 8;
        }
        return this.recordWidth;
    }

    public int getMediaCodecHeight() {
        Rect rect = this.cropRegionRect;
        if (rect != null) {
            return (((int) (rect.height() * getCropRegionScale())) / 8) * 8;
        }
        return this.recordHeight;
    }

    private float getCropRegionScale() {
        return Math.max((this.recordWidth * 1.0f) / Math.max(this.displayWidth, this.recordWidth), (this.recordHeight * 1.0f) / Math.max(this.displayHeight, this.recordHeight));
    }

    public int getBitrate() {
        return this.bitrate;
    }

    public void setBitrate(int i) {
        this.bitrate = i;
    }

    public int getBitrateMode() {
        return this.bitrateMode;
    }

    public void setBitrateMode(int i) {
        this.bitrateMode = i;
    }

    public int getRecordWidth() {
        return this.recordWidth;
    }

    public void setRecordWidth(int i) {
        this.recordWidth = i;
    }

    public int getRecordHeight() {
        return this.recordHeight;
    }

    public void setRecordHeight(int i) {
        this.recordHeight = i;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

    public void setFrameRate(int i) {
        this.frameRate = i;
    }

    public void setDisplayWidth(int i) {
        this.displayWidth = i;
    }

    public void setDisplayHeight(int i) {
        this.displayHeight = i;
    }

    public int getDisplayWidth() {
        return this.displayWidth;
    }

    public int getDisplayHeight() {
        return this.displayHeight;
    }

    public int getIframeInterval() {
        return this.iframeInterval;
    }

    public void setIframeInterval(int i) {
        this.iframeInterval = i;
    }

    public String toString() {
        return "CodecParameter{rect=" + this.cropRegionRect + ", bitrate=" + this.bitrate + ", bitrateMode=" + this.bitrateMode + ", frameRate=" + this.frameRate + ", recordWidth=" + this.recordWidth + ", recordHeight=" + this.recordHeight + ", iframeInterval=" + this.iframeInterval + '}';
    }
}
