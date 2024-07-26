package com.nothing.commonutils.utils.mediacodec.entity;

import android.graphics.Point;
import com.nothing.commonutils.utils.ByteUtils;

public class H264Header {
    public static final byte FRAME_TYPE_B = 2;
    public static final byte FRAME_TYPE_IDR = 1;
    public static final byte FRAME_TYPE_INFO = 3;
    public static final byte FRAME_TYPE_P = 0;
    public static final int H264_HEADER_SIZE = 20;
    private int height;
    private int width;
    protected final int NATIVE_STREAM_ID = 32767;
    private byte[] data = new byte[20];
    private int stream_id = 32767;
    private int bpp = 16;
    private short frame_id = 0;
    private byte frame_type = 0;

    public H264Header(Point point) {
        this.width = point.x;
        this.height = point.y;
        fillData();
    }

    public void setData(int i, int i2, int i3, int i4) {
        this.data = new byte[20];
        this.stream_id = 32767;
        this.width = i;
        this.height = i2;
        this.bpp = i3;
        this.frame_id = (short) 0;
        this.frame_type = i4 != 0 ? (byte) 1 : (byte) 0;
        fillData();
    }

    private void fillData() {
        ByteUtils.intToBytes(this.stream_id, this.data, 0);
        ByteUtils.intToBytes(this.width, this.data, 4);
        ByteUtils.intToBytes(this.height, this.data, 8);
        ByteUtils.intToBytes(this.bpp, this.data, 12);
        byte[] shortToBytes = ByteUtils.shortToBytes(this.frame_id);
        System.arraycopy(shortToBytes, 0, this.data, 16, shortToBytes.length);
        byte[] bArr = this.data;
        bArr[18] = this.frame_type;
        bArr[19] = 0;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getDataLength() {
        return this.data.length;
    }
}
