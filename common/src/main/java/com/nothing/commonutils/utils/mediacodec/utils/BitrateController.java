package com.nothing.commonutils.utils.mediacodec.utils;

import android.media.MediaCodec;
import android.os.Bundle;

import com.nothing.commonutils.utils.Lg;

public class BitrateController {
    int mLastStep = 0;
    float mLastQuality = 0.25f;

    private static final String TAG = "BitrateController";
    private int adjustStep(int i, int i2, boolean z) {
        int i3 = i + (i2 >= 20 ? z ? 1 : 0 : i2 >= 16 ? -2 : i2 >= 12 ? -3 : i2 >= 8 ? -4 : -5);
        int i4 = i3 >= 8 ? i3 : 8;
        if (i4 > 25) {
            return 25;
        }
        return i4;
    }

    public static void setBitrateOnFly(MediaCodec mediaCodec, int i) {
        if (mediaCodec == null) {
            return;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("video-bitrate", i);
            mediaCodec.setParameters(bundle);
        } catch (Exception e) {
            Lg.e(TAG, "set bitrate failed");
            e.printStackTrace();
        }
    }

    public static void requestKeyFrameSoon(MediaCodec mediaCodec) {
        if (mediaCodec == null) {
            return;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("request-sync", 0);
            mediaCodec.setParameters(bundle);
            Lg.i(TAG, "request sync keyframe");
        } catch (Exception e) {
            Lg.e(TAG, "request sync keyframe failed");
            e.printStackTrace();
        }
    }

    public int calcBitrate(int i, int i2, int i3, int i4) {
        return (((int) (i * calcQuality(i2, i3, i4))) / 2) * 2;
    }

    private float calcQuality(int i, int i2, int i3) {
        int calcStep2 = calcStep2(i, i2, i3);
        if (this.mLastStep == 0) {
            this.mLastStep = calcStep2;
        }
        int adjustStep = adjustStep(this.mLastStep, calcStep2, true);
        this.mLastStep = adjustStep;
        float f = (float) ((adjustStep * 5.0d) / 100.0d);
        this.mLastQuality = f;
        return f;
    }

    private int calcStep2(int i, int i2, int i3) {
        int i4 = i3 + i2;
        int i5 = i / 2;
        return i4 > i5 ? (int) ((((i2 + 1) * 100.0f) / i4) / 5.0f) : (int) ((((i2 + 1) * 100.0f) / i5) / 5.0f);
    }
}
