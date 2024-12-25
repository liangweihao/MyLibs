package com.nothing.commonutils.utils;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Size;

/**
 * --------------------
 * <p>Author：
 * lwh
 * <p>Created Time:
 * 2024/12/25
 * <p>Intro:
 *
 * <p>Thinking:
 *
 * <p>Problem:
 *
 * <p>Attention:
 * --------------------
 */

public class SizeUtils {

    /**
     * 根据给定的原始尺寸和目标尺寸，按照等比缩放的原则计算出缩放后的尺寸，使其尽可能填充目标尺寸
     *
     * @param sourceWidth  原始宽度
     * @param sourceHeight 原始高度
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 包含缩放后宽度和高度的数组，数组第一个元素为缩放后宽度，第二个元素为缩放后高度
     * @like 1280 720 1920 1080 -> 1920 1080
     * @like 2160 1080 1920 1080 -> 1920 960
     * @like 1800 1000 1920 1080 -> 1920 1067
     */
    public static Size scaleToFill(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight
    ) {
        float scaleX = (float) targetWidth / sourceWidth;
        float scaleY = (float) targetHeight / sourceHeight;
        float scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) Math.floor(sourceWidth * scale);
        int scaledHeight = (int) Math.floor(sourceHeight * scale);
        return new Size(scaledWidth, scaledHeight);
    }

    public static RectF center(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight
    ) {
        Matrix mMatrix = new Matrix();
        mMatrix.setTranslate(
                Math.round((targetWidth - sourceWidth) * 0.5f),
                Math.round((targetHeight - sourceHeight) * 0.5f)
        );
        RectF sourceRect = new RectF(0, 0, sourceWidth, sourceHeight);
        // 创建用于存储变换后（居中后）的Rect对象
        RectF centeredRect = new RectF();
        // 使用Matrix的mapRect方法将源Rect根据设置的变换矩阵进行变换，得到居中后的Rect位置
        mMatrix.mapRect(centeredRect, sourceRect);
        return centeredRect;
    }


    public static RectF centerCrop(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight
    ) {
        float scale;
        float dx = 0, dy = 0;
        Matrix mMatrix = new Matrix();

        if (sourceWidth * targetHeight > targetWidth * sourceHeight) {
            scale = (float) targetHeight / (float) sourceHeight;
            dx = (targetWidth - sourceWidth * scale) * 0.5f;
        } else {
            scale = (float) targetWidth / (float) sourceWidth;
            dy = (targetHeight - sourceHeight * scale) * 0.5f;
        }

        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(Math.round(dx), Math.round(dy));

        // 创建一个代表源图像区域的RectF（这里坐标简单假设从(0,0)开始，宽高为前面定义的值）
        RectF sourceRectF = new RectF(0, 0, sourceWidth, sourceHeight);
        // 创建一个代表目标显示区域的RectF（坐标也简单假设从(0,0)开始，宽高为目标区域的值）
        RectF targetRectF = new RectF(0, 0, targetWidth, targetHeight);

        // 使用变换矩阵对源图像区域进行变换，使其符合居中裁剪的效果
        mMatrix.mapRect(targetRectF, sourceRectF);
        return targetRectF;
    }


    public static RectF centerFit(int sourceWidth,
                                  int sourceHeight,
                                  int targetWidth,
                                  int targetHeight){
        Matrix mDrawMatrix = new Matrix();
        final RectF mTempSrc = new RectF();
        final RectF mTempDst = new RectF();
        mTempSrc.set(0, 0, sourceWidth, sourceHeight);
        mTempDst.set(0, 0, targetWidth, targetHeight);
        mDrawMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
        // 创建一个新的RectF对象用于接收变换后的结果
        RectF resultRectF = new RectF();
        mDrawMatrix.mapRect(resultRectF, mTempSrc);
        return resultRectF;
    }

}
