package com.nothing.commonutils.utils.mediacodec.entity;

import android.graphics.PixelFormat;
import com.nothing.commonutils.utils.ByteUtils;

public class ScreenShotInfo {
    private static final int INFO_SIZE = 16;
    private int bpp;
    private byte[] data = new byte[16];
    private int format;
    private int height;
    private int width;

    public ScreenShotInfo(int i, int i2, int i3) {
        this.width = i;
        this.height = i2;
        this.bpp = convertBpp(i3);
        this.format = i3;
        fillData();
    }

    private void fillData() {
        ByteUtils.intToBytes(this.width, this.data, 0);
        ByteUtils.intToBytes(this.height, this.data, 4);
        ByteUtils.intToBytes(this.bpp, this.data, 8);
        ByteUtils.intToBytes(this.format, this.data, 12);
    }

    public byte[] getData() {
        return this.data;
    }

    public int getLength() {
        return this.data.length;
    }

    private int convertBpp(int i) {
        try {
            PixelFormat pixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(i, pixelFormat);
            return pixelFormat.bitsPerPixel;
        } catch (Exception e) {
            e.printStackTrace();
            return 32;
        }
    }
}
