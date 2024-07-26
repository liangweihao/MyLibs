package com.nothing.commonutils.utils.mediacodec.entity;

import com.nothing.commonutils.utils.ByteUtils;

public class DisplayInfo {
    protected static final int DISPLAY_INFO_OFFSET_H = 4;
    protected static final int DISPLAY_INFO_OFFSET_O = 8;
    protected static final int DISPLAY_INFO_SIZE = 12;
    private byte[] data = new byte[12];
    private int height;
    private int orientation;
    private int width;

    public DisplayInfo(int width, int height, int orientation) {
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        fillData();
    }

    private void fillData() {
        ByteUtils.intToBytes(this.width, this.data, 0);
        ByteUtils.intToBytes(this.height, this.data, 4);
        ByteUtils.intToBytes(this.orientation, this.data, 8);
    }

    public byte[] getData() {
        return this.data;
    }

    public int getDataLength() {
        return this.data.length;
    }
}
