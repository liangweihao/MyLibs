package com.nothing.commonutils.utils;

public class ByteUtils {
    public static byte[] intToBytes(int i) {
        return new byte[]{(byte) (i & 255), (byte) ((i >> 8) & 255), (byte) ((i >> 16) &
                                                                             255), (byte) (
                (i >> 24) & 255)};
    }

    public static int bytesToInt(byte[] bArr, int i) {
        return ((bArr[i + 3] & 255) << 24) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) |
               ((bArr[i + 2] & 255) << 16);
    }

    public static void intToBytes(int i, byte[] bArr, int i2) {
        bArr[i2 + 3] = (byte) ((i >> 24) & 255);
        bArr[i2 + 2] = (byte) ((i >> 16) & 255);
        bArr[i2 + 1] = (byte) ((i >> 8) & 255);
        bArr[i2 + 0] = (byte) (i & 255);
    }

    public static byte[] shortToBytes(short s) {
        byte[] bArr = new byte[2];
        int i = 0;
        int i2 = s;
        while (i < 2) {
            bArr[i] = new Integer(i2 & 255).byteValue();
            i++;
            i2 >>= 8;
        }
        return bArr;
    }

    public static short bytesToShort(byte[] bArr) {
        return (short) (((short) (((short) (bArr[1] & 255)) << 8)) | ((short) (bArr[0] & 255)));
    }

    public static byte[] longToBytes(long j) {
        byte[] bArr = new byte[8];
        int i = 0;
        while (i < 8) {
            int i2 = i + 1;
            bArr[i] = (byte) ((j >> (64 - (i2 * 8))) & 255);
            i       = i2;
        }
        return bArr;
    }

    public static long bytesToLong(byte[] bArr) {
        long j = 0;
        for (int i = 0; i < 8; i++) {
            j = (j << 8) | (bArr[i] & 255);
        }
        return j;
    }

    public static int getByteIndexOf(byte[] bArr, byte[] bArr2, int i) {
        return getByteIndexOf(bArr, bArr2, i, bArr.length);
    }

    public static int getByteIndexOf(byte[] bArr, byte[] bArr2, int i, int i2) {
        if (bArr != null && bArr2 != null && bArr.length != 0 && bArr2.length != 0) {
            if (i2 > bArr.length) {
                i2 = bArr.length;
            }
            while (i < i2) {
                if (bArr[i] == bArr2[0] && bArr2.length + i < i2) {
                    int i3 = 1;
                    while (i3 < bArr2.length && bArr[i + i3] == bArr2[i3]) {
                        i3++;
                    }
                    if (i3 == bArr2.length) {
                        return i;
                    }
                }
                i++;
            }
        }
        return -1;
    }
}
