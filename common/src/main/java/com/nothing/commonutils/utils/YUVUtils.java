package com.nothing.commonutils.utils;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.Image.Plane;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import androidx.annotation.Nullable;

public class YUVUtils {

    //COLOR_FormatYUV420PackedPlanar
    public static byte[] convertYUV420PPToYUV(Image image) {
        // 获取 Y, U, V 平面
        Plane[] planes = image.getPlanes();

        // 获取 Y 平面数据
        ByteBuffer yBuffer = planes[0].getBuffer();
        byte[] yData = new byte[yBuffer.remaining()];
        yBuffer.get(yData);

        // 获取 U 平面数据
        ByteBuffer uBuffer = planes[1].getBuffer();
        byte[] uData = new byte[uBuffer.remaining()];
        uBuffer.get(uData);

        // 获取 V 平面数据
        ByteBuffer vBuffer = planes[2].getBuffer();
        byte[] vData = new byte[vBuffer.remaining()];
        vBuffer.get(vData);

        // 获取图像的宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();

        // 创建一个字节数组来存储合并后的 YUV 数据
        byte[] yuvData = new byte[width * height + (width / 2) * (height / 2) * 2];

        // 将 YUV 数据合并
        System.arraycopy(yData, 0, yuvData, 0, yData.length);
        System.arraycopy(uData, 0, yuvData, yData.length, uData.length);
        System.arraycopy(vData, 0, yuvData, yData.length + uData.length, vData.length);

        return yuvData;
    }


    //COLOR_FormatYUV420PackedSemiPlanar
    public static byte[] convertYUV420PSPToYUV(Image image) {
        // 获取 Y, U, V 平面
        Plane[] planes = image.getPlanes();

        // 获取 Y 平面数据
        ByteBuffer yBuffer = planes[0].getBuffer();
        byte[] yData = new byte[yBuffer.remaining()];
        yBuffer.get(yData);

        // 获取 U 平面数据
        ByteBuffer uBuffer = planes[1].getBuffer();
        byte[] uData = new byte[uBuffer.remaining()];
        uBuffer.get(uData);

        // 获取 V 平面数据
        ByteBuffer vBuffer = planes[2].getBuffer();
        byte[] vData = new byte[vBuffer.remaining()];
        vBuffer.get(vData);

        // 获取图像的宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();

        // 创建一个字节数组来存储合并后的 YUV 数据
        byte[] yuvData = new byte[width * height + (width / 2) * (height / 2) * 2];

        // 将 YUV 数据合并
        System.arraycopy(yData, 0, yuvData, 0, yData.length);
        System.arraycopy(uData, 0, yuvData, yData.length, uData.length);
        System.arraycopy(vData, 0, yuvData, yData.length + uData.length, vData.length);

        return yuvData;
    }
    //COLOR_FormatYUV420SemiPlanar
    public static byte[] convertYUV420SPToYUV(Image image) {
        // 获取 Y, UV 平面
        Plane[] planes = image.getPlanes();

        // 获取 Y 平面数据
        ByteBuffer yBuffer = planes[0].getBuffer();
        byte[] yData = new byte[yBuffer.remaining()];
        yBuffer.get(yData);

        // 获取 UV 平面数据
        ByteBuffer uvBuffer = planes[1].getBuffer();
        byte[] uvData = new byte[uvBuffer.remaining()];
        uvBuffer.get(uvData);

        // 获取图像的宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();

        // 创建一个字节数组来存储合并后的 YUV 数据
        byte[] yuvData = new byte[width * height + (width / 2) * (height / 2) * 2];

        // 将 Y 数据填充到 YUV 数据中
        System.arraycopy(yData, 0, yuvData, 0, yData.length);

        // 将 UV 数据填充到 YUV 数据中
        System.arraycopy(uvData, 0, yuvData, yData.length, uvData.length);

        return yuvData;
    }



    //COLOR_FormatYUV420Planar
    public static byte[] convertYUV420PToByteArray(Image image) {
        // 获取 YUV 数据的三个平面
        Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();  // Y 平面
        ByteBuffer uBuffer = planes[1].getBuffer();  // U 平面
        ByteBuffer vBuffer = planes[2].getBuffer();  // V 平面

        // 获取每个平面的数据长度
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        // 将 YUV 数据拼接成一个字节数组
        byte[] yuvData = new byte[ySize + uSize + vSize];
        yBuffer.get(yuvData, 0, ySize);
        uBuffer.get(yuvData, ySize, uSize);
        vBuffer.get(yuvData, ySize + uSize, vSize);
        return yuvData;
    }

    public static  byte[] convertYUV422ToByteArray(Image image) {
        // 获取图像的平面（Y、U、V）
        Plane[] planes = image.getPlanes();
        // 获取 Y 分量
        Plane yPlane = planes[0];
        ByteBuffer yBuffer = yPlane.getBuffer();
        byte[] yBytes = new byte[yBuffer.remaining()];
        yBuffer.get(yBytes);
        // 获取 U 分量
        Plane uPlane = planes[1];
        ByteBuffer uBuffer = uPlane.getBuffer();
        byte[] uBytes = new byte[uBuffer.remaining()];
        uBuffer.get(uBytes);
        // 获取 V 分量
        Plane vPlane = planes[2];
        ByteBuffer vBuffer = vPlane.getBuffer();
        byte[] vBytes = new byte[vBuffer.remaining()];
        vBuffer.get(vBytes);
        // 获取图像的宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();
        // 构建最终的 YUV 数组
        byte[] yuvArray = new byte[width * height * 3 / 2]; // YUV 4:2:2 格式
        // 将 Y 分量复制到 YUV 数组
        System.arraycopy(yBytes, 0, yuvArray, 0, yBytes.length);
        // 将 U 和 V 分量交替存储
        int uvIndex = yBytes.length;
        for (int i = 0; i < uBytes.length; i++) {
            yuvArray[uvIndex + 2 * i] = uBytes[i];
            yuvArray[uvIndex + 2 * i + 1] = vBytes[i];
        }
        // 返回构建的 YUV 数组
        return yuvArray;
    }

    public static byte[] convertYUV444ToByteArray(Image image) {
        // 获取图像的平面（Y、U、V）
        Plane[] planes = image.getPlanes();

        // 获取 Y 分量
        Plane yPlane = planes[0];
        ByteBuffer yBuffer = yPlane.getBuffer();
        byte[] yBytes = new byte[yBuffer.remaining()];
        yBuffer.get(yBytes);

        // 获取 U 分量
        Plane uPlane = planes[1];
        ByteBuffer uBuffer = uPlane.getBuffer();
        byte[] uBytes = new byte[uBuffer.remaining()];
        uBuffer.get(uBytes);

        // 获取 V 分量
        Plane vPlane = planes[2];
        ByteBuffer vBuffer = vPlane.getBuffer();
        byte[] vBytes = new byte[vBuffer.remaining()];
        vBuffer.get(vBytes);

        // 获取图像的宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();

        // 构建最终的 YUV 数组
        byte[] yuvArray = new byte[width * height * 3]; // YUV 4:4:4 格式需要 3 个分量

        // 将 Y 分量复制到 YUV 数组
        System.arraycopy(yBytes, 0, yuvArray, 0, yBytes.length);

        // 将 U 分量复制到 YUV 数组
        int uvIndex = yBytes.length;
        System.arraycopy(uBytes, 0, yuvArray, uvIndex, uBytes.length);

        // 将 V 分量复制到 YUV 数组
        int uvIndexV = uvIndex + uBytes.length;
        System.arraycopy(vBytes, 0, yuvArray, uvIndexV, vBytes.length);

        // 返回构建的 YUV 数组
        return yuvArray;
    }


    @Nullable
    public static Bitmap compressYuvImageToJpeg(byte[] yuvData,int width,int height){
        // 使用 YuvImage 将 YUV 数据转换为 JPEG 数据
        YuvImage yuvImage = new YuvImage(yuvData, android.graphics.ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean success = yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, outputStream);

        if (success) {
            // 从 ByteArrayOutputStream 获取 JPEG 数据并解码为 Bitmap
            byte[] jpegData = outputStream.toByteArray();
            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        } else {
            Log.e("YUVToBitmap", "YUV to JPEG conversion failed");
            return null;
        }
    }

    // 将 YUV420SemiPlanar 格式的数据转换为 Bitmap
    @Nullable
    public static Bitmap convertYUV420SPToBitmap(Image image) {
        try {
            Plane[] planes = image.getPlanes();

            // 获取 Y 平面数据
            ByteBuffer yBuffer = planes[0].getBuffer();
            byte[] yData = new byte[yBuffer.remaining()];
            yBuffer.get(yData);

            // 获取 UV 平面数据（交替存储 U 和 V）
            ByteBuffer uvBuffer = planes[1].getBuffer();
            byte[] uvData = new byte[uvBuffer.remaining()];
            uvBuffer.get(uvData);

            int width = image.getWidth();
            int height = image.getHeight();

            // 创建一个 RGB 数组来存储转换后的像素数据
            int[] rgbData = new int[width * height];

            // 将 YUV 转换为 RGB
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int yIndex = i * width + j;

                    // UV 分量的索引位置（UV 数据是交替存储的）
                    int uvIndex = (i / 2) * (width / 2) * 2 + (j / 2) * 2;

                    // 获取 Y, U, V 值
                    int y = yData[yIndex] & 0xFF;
                    int u = uvData[uvIndex] & 0xFF;
                    int v = uvData[uvIndex + 1] & 0xFF;

                    // YUV -> RGB 转换
                    int r = (int) (y + 1.402 * (v - 128));
                    int g = (int) (y - 0.344136 * (u - 128) - 0.714136 * (v - 128));
                    int b = (int) (y + 1.772 * (u - 128));

                    // 限制 RGB 值在 0 到 255 之间
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));

                    // 合并 RGB 值为一个像素，并存入 rgbData 数组
                    rgbData[i * width + j] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                }
            }
            // 创建 Bitmap
            return Bitmap.createBitmap(rgbData, width, height, Config.ARGB_8888);
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    // yuv420PSP
    @Nullable
    public static Bitmap convertYUV420PSPToBitmap(Image image) {
        try {
            // 获取 Y, UV 平面数据
            Plane[] planes = image.getPlanes();

            // 获取 Y 分量数据
            ByteBuffer yBuffer = planes[0].getBuffer();
            byte[] yData = new byte[yBuffer.remaining()];
            yBuffer.get(yData);

            // 获取 UV 平面数据
            ByteBuffer uvBuffer = planes[1].getBuffer();
            byte[] uvData = new byte[uvBuffer.remaining()];
            uvBuffer.get(uvData);

            // 获取图像的宽度和高度
            int width = image.getWidth();
            int height = image.getHeight();

            // 将 YUV 数据转换为 RGB
            int[] rgbData = new int[width * height];

            // YUV -> RGB 转换公式
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int yIndex = i * width + j;

                    // UV 分量索引，每 2x2 像素共享一个 UV 值
                    int uvIndex = ((i / 2) * (width / 2) + (j / 2)) * 2;

                    // Y, U, V 值
                    int y = yData[yIndex] & 0xFF;
                    int u = uvData[uvIndex] & 0xFF;
                    int v = uvData[uvIndex + 1] & 0xFF;

                    // RGB 转换
                    int r = (int)(y + 1.402 * (v - 128));
                    int g = (int)(y - 0.344136 * (u - 128) - 0.714136 * (v - 128));
                    int b = (int)(y + 1.772 * (u - 128));

                    // 限制 RGB 值在 0 到 255 之间
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));

                    // 将 RGB 值合并为一个像素
                    rgbData[i * width + j] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                }
            }
            // 创建 Bitmap
            return Bitmap.createBitmap(rgbData, width, height, Config.ARGB_8888);
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;

    }


    // yuv420P
    @Nullable
    public static Bitmap convertYUV420PToBitmap(Image image) {
        try {
            // 获取 Y, U, V 平面数据
            Plane[] planes = image.getPlanes();

            // 获取 Y 分量数据
            ByteBuffer yBuffer = planes[0].getBuffer();
            byte[] yData = new byte[yBuffer.remaining()];
            yBuffer.get(yData);

            // 获取 U 分量数据
            ByteBuffer uBuffer = planes[1].getBuffer();
            byte[] uData = new byte[uBuffer.remaining()];
            uBuffer.get(uData);

            // 获取 V 分量数据
            ByteBuffer vBuffer = planes[2].getBuffer();
            byte[] vData = new byte[vBuffer.remaining()];
            vBuffer.get(vData);

            // 获取图像的宽度和高度
            int width = image.getWidth();
            int height = image.getHeight();

            // 将 YUV 数据转换为 RGB
            int[] rgbData = new int[width * height];

            // YUV -> RGB 转换公式
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int yIndex = i * width + j;
                    int uIndex = (i / 2) * (width / 2) + (j / 2);
                    int vIndex = (i / 2) * (width / 2) + (j / 2);

                    // Y, U, V 值
                    int y = yData[yIndex] & 0xFF;
                    int u = uData[uIndex] & 0xFF;
                    int v = vData[vIndex] & 0xFF;

                    // RGB 转换
                    int r = (int)(y + 1.402 * (v - 128));
                    int g = (int)(y - 0.344136 * (u - 128) - 0.714136 * (v - 128));
                    int b = (int)(y + 1.772 * (u - 128));

                    // 限制 RGB 值在 0 到 255 之间
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));

                    // 将 RGB 值合并为一个像素
                    rgbData[i * width + j] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                }
            }

            // 创建 Bitmap

            return Bitmap.createBitmap(rgbData, width, height, Config.ARGB_8888);
        }catch (Throwable e){
            e.printStackTrace();
        }
        return  null;
    }

    // 将 YUV420P 格式的字节数组转换为 Bitmap
    public static Bitmap convertYUV420PToBitmap(byte[] yuv420pData, int width, int height) {
        // YUV420P 格式：Y平面、U平面、V平面
        int ySize = width * height;
        int uvSize = width * height / 4;

        // 提取 Y、U、V 分量
        byte[] yPlane = new byte[ySize];
        byte[] uPlane = new byte[uvSize];
        byte[] vPlane = new byte[uvSize];

        System.arraycopy(yuv420pData, 0, yPlane, 0, ySize);
        System.arraycopy(yuv420pData, ySize, uPlane, 0, uvSize);
        System.arraycopy(yuv420pData, ySize + uvSize, vPlane, 0, uvSize);

        // 创建一个 RGB 数组来存储转换后的像素数据
        int[] rgbData = new int[width * height];

        // 将 YUV 转换为 RGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int yIndex = y * width + x;

                // 获取 Y、U、V 值
                int yValue = yPlane[yIndex] & 0xFF;
                int uIndex = (y / 2) * (width / 2) + (x / 2);  // UV 坐标为 2x2 子采样
                int uValue = uPlane[uIndex] & 0xFF;
                int vValue = vPlane[uIndex] & 0xFF;

                // YUV -> RGB 转换
                int r = (int)(yValue + 1.402 * (vValue - 128));
                int g = (int)(yValue - 0.344136 * (uValue - 128) - 0.714136 * (vValue - 128));
                int b = (int)(yValue + 1.772 * (uValue - 128));

                // 限制 RGB 值在 0 到 255 之间
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                // 合并 RGB 值为一个像素，并存入 rgbData 数组
                rgbData[y * width + x] = (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        }

        // 创建 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(rgbData, width, height, Config.ARGB_8888);

        return bitmap;
    }




    public static Bitmap convertYUV422ToBitmap(byte[] yuvData, int width, int height) {
        // 创建一个空的 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        // Y、U、V 分量的数据
        byte[] yData = new byte[width * height];
        byte[] uvData = new byte[(width / 2) * height * 2]; // 4:2:2 格式中 UV 共享

        // 填充 Y、U 和 V 数据
        System.arraycopy(yuvData, 0, yData, 0, width * height);
        System.arraycopy(yuvData, width * height, uvData, 0, uvData.length);

        // 遍历每个像素并将 YUV 转换为 RGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int yIndex = y * width + x;
                int uvIndex = (y * (width / 2) + (x / 2)) * 2;

                int Y = yData[yIndex] & 0xFF;  // Y 值
                int U = uvData[uvIndex] & 0xFF;  // U 值
                int V = uvData[uvIndex + 1] & 0xFF;  // V 值

                // YUV 到 RGB 的转换公式
                int R = (int) (Y + 1.402 * (V - 128));
                int G = (int) (Y - 0.344136 * (U - 128) - 0.714136 * (V - 128));
                int B = (int) (Y + 1.772 * (U - 128));

                // 限制 RGB 值在 0-255 的范围内
                R = Math.min(Math.max(R, 0), 255);
                G = Math.min(Math.max(G, 0), 255);
                B = Math.min(Math.max(B, 0), 255);

                // 设置 Bitmap 像素
                bitmap.setPixel(x, y, (0xFF << 24) | (R << 16) | (G << 8) | B);
            }
        }

        return bitmap;
    }

    public static Bitmap convertYUV444ToBitmap(byte[] yuvData, int width, int height) {
        // 创建一个空的 Bitmap，大小为图像的宽度和高度
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        // 获取 YUV 分量的数据
        byte[] yData = new byte[width * height];
        byte[] uData = new byte[width * height];
        byte[] vData = new byte[width * height];

        // 填充 Y、U 和 V 数据
        System.arraycopy(yuvData, 0, yData, 0, width * height);
        System.arraycopy(yuvData, width * height, uData, 0, width * height);
        System.arraycopy(yuvData, width * height * 2, vData, 0, width * height);

        // 遍历每个像素并将 YUV 转换为 RGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 获取当前像素的 Y、U 和 V 分量
                int yIndex = y * width + x;
                int uIndex = yIndex;  // YUV 4:4:4 格式中的 U 和 V 分量是按像素存储的
                int vIndex = yIndex;

                int Y = yData[yIndex] & 0xFF;  // Y 值，确保为无符号
                int U = uData[uIndex] & 0xFF;  // U 值
                int V = vData[vIndex] & 0xFF;  // V 值

                // YUV 到 RGB 的转换公式
                int R = (int) (Y + 1.402 * (V - 128));
                int G = (int) (Y - 0.344136 * (U - 128) - 0.714136 * (V - 128));
                int B = (int) (Y + 1.772 * (U - 128));

                // 限制 RGB 值在 0-255 的范围内
                R = Math.min(Math.max(R, 0), 255);
                G = Math.min(Math.max(G, 0), 255);
                B = Math.min(Math.max(B, 0), 255);

                // 设置 Bitmap 像素
                bitmap.setPixel(x, y, (0xFF << 24) | (R << 16) | (G << 8) | B);
            }
        }

        return bitmap;
    }


    public static int yuvToRgb(int y, int u, int v) {
        int r = (int) (y + 1.402 * (v - 128));
        int g = (int) (y - 0.344136 * (u - 128) - 0.714136 * (v - 128));
        int b = (int) (y + 1.772 * (u - 128));

        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));

        return (r << 16) | (g << 8) | b;
    }



}
