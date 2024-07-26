package com.nothing.commonutils.utils.mediacodec.interfaces;

import android.graphics.Point;

public interface IDisplayInfo {
    int getDisplayRotation();

    Point getDisplaySize();

    int getOrientation();

    Point getScreenSize();
}
