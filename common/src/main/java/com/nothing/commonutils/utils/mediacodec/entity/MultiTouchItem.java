package com.nothing.commonutils.utils.mediacodec.entity;

public class MultiTouchItem {
    private final int mAction;
    private final float mPressure;
    private final int mTrackId;
    private final float mWidth;
    private final float mX;
    private final float mY;

    public MultiTouchItem() {
        this.mTrackId = 0;
        this.mAction = 0;
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mPressure = 1.0f;
        this.mWidth = 1.0f;
    }

    public MultiTouchItem(int i, int i2, float f, float f2) {
        this.mTrackId = 0;
        this.mAction = 0;
        this.mX = f;
        this.mY = f2;
        this.mPressure = 1.0f;
        this.mWidth = 1.0f;
    }

    public static int findIndex(MultiTouchItem[] multiTouchItemArr, int i) {
        for (int i2 = 0; i2 < multiTouchItemArr.length; i2++) {
            if (multiTouchItemArr[i2].getTrackId() == i) {
                return i2;
            }
        }
        return -1;
    }

    public int getTrackId() {
        return this.mTrackId;
    }

    public int getAction() {
        return this.mAction;
    }

    public float getX() {
        return this.mX;
    }

    public float getY() {
        return this.mY;
    }

    public float getPressure() {
        return this.mPressure;
    }

    public float getWidth() {
        return this.mWidth;
    }
}
