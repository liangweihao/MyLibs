package com.nothing.commonutils.utils.mediacodec.interfaces;


public interface OnFrameCallBack {
    boolean canUpdateFrame();

    void onUpdateFrame();
}
