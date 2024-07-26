package com.nothing.commonutils.utils.mediacodec.interfaces;

public interface OnScreenShareCallback {
    long onClearNeeded(int totalFrameCount, int keyFrameCount);

    boolean onGetWriteState();

    void onH264Info(byte[] data, boolean isKeyFrame);
}
