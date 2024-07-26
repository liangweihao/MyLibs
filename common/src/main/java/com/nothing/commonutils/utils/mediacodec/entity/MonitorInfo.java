package com.nothing.commonutils.utils.mediacodec.entity;

import com.nothing.commonutils.utils.ByteUtils;

public class MonitorInfo {
    protected static final int MONITOR_INFO_OFFSET_BPP = 148;
    protected static final int MONITOR_INFO_OFFSET_NAME = 4;
    protected static final int MONITOR_INFO_OFFSET_RECT = 132;
    protected static final int MONITOR_INFO_SIZE = 152;
    private int bpp;
    private byte[] data = new byte[MONITOR_INFO_SIZE];
    private int index;
    private String name;
    private TagRect rect;

    /* JADX INFO: Access modifiers changed from: package-private */
    
    public class TagRect {
        public int bottom;
        private byte[] data;
        public int left;
        public int right;
        public int top;

        public TagRect() {
            this.left = 0;
            this.top = 0;
            this.right = 0;
            this.bottom = 0;
            this.data = new byte[16];
        }

        public TagRect(int i, int i2, int i3, int i4) {
            this.data = new byte[16];
            this.left = i;
            this.top = i2;
            this.right = i3;
            this.bottom = i4;
            fillData();
        }

        private void fillData() {
            ByteUtils.intToBytes(this.left, this.data, 0);
            ByteUtils.intToBytes(this.top, this.data, 4);
            ByteUtils.intToBytes(this.right, this.data, 8);
            ByteUtils.intToBytes(this.bottom, this.data, 12);
        }

        public byte[] getData() {
            return this.data;
        }

        public int getLength() {
            return this.data.length;
        }
    }

    public MonitorInfo(int i, String str, int i2, int i3, int i4, int i5, int i6) {
        this.rect = new TagRect(i2, i3, i4, i5);
        this.index = i;
        this.name = str;
        this.bpp = i6;
        fillData();
    }

    private void fillData() {
        ByteUtils.intToBytes(this.index, this.data, 0);
        byte[] bytes = this.name.getBytes();
        System.arraycopy(bytes, 0, this.data, 4, bytes.length);
        byte[] data = this.rect.getData();
        System.arraycopy(data, 0, this.data, MONITOR_INFO_OFFSET_RECT, data.length);
        ByteUtils.intToBytes(this.bpp, this.data, MONITOR_INFO_OFFSET_BPP);
    }

    public byte[] getData() {
        return this.data;
    }

    public int getDataLength() {
        return this.data.length;
    }
}
