package com.nothing.myserver;


import android.os.Environment;
import android.os.StatFs;

import com.nothing.commonutils.utils.Lg;

import java.io.File;

public class DeviceUtils {

    /**
     * 获取内部存储总容量
     *
     * @return 内部存储总容量，单位为字节
     */
    public static long getTotalInternalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    /**
     * 获取内部存储可用容量
     *
     * @return 内部存储可用容量，单位为字节
     */
    public static long getAvailableInternalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * 检查外部存储是否可用
     *
     * @return 外部存储是否可用
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取外部存储总容量
     *
     * @return 外部存储总容量，单位为字节
     */
    public static long getTotalExternalStorage() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    /**
     * 获取外部存储可用容量
     *
     * @return 外部存储可用容量，单位为字节
     */
    public static long getAvailableExternalStorage() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * 获取总 RAM 容量
     *
     * @return 总 RAM 容量，单位为字节
     */
    public static long getTotalRam() {
        try {
            Class<?> activityManagerNativeClass =
                    Class.forName("android.app.ActivityManagerNative");
            java.lang.reflect.Method getDefaultMethod =
                    activityManagerNativeClass.getMethod("getDefault");
            Object activityManagerNative = getDefaultMethod.invoke(null);
            java.lang.reflect.Method getMemoryInfoMethod = activityManagerNativeClass.getMethod(
                    "getMemoryInfo",
                    Class.forName("android.app.ActivityManager$MemoryInfo")
            );
            Class<?> memoryInfoClass = Class.forName("android.app.ActivityManager$MemoryInfo");
            Object memoryInfo = memoryInfoClass.newInstance();
            getMemoryInfoMethod.invoke(activityManagerNative, memoryInfo);
            java.lang.reflect.Method getTotalMemMethod = memoryInfoClass.getMethod("getTotalMem");
            return (long) getTotalMemMethod.invoke(memoryInfo);
        } catch (Exception e) {
            Lg.e("MyHttpServer", "Error getting total RAM: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 获取可用 RAM 容量
     *
     * @return 可用 RAM 容量，单位为字节
     */
    public static long getAvailableRam() {
        try {
            Class<?> activityManagerNativeClass =
                    Class.forName("android.app.ActivityManagerNative");
            java.lang.reflect.Method getDefaultMethod =
                    activityManagerNativeClass.getMethod("getDefault");
            Object activityManagerNative = getDefaultMethod.invoke(null);
            java.lang.reflect.Method getMemoryInfoMethod = activityManagerNativeClass.getMethod(
                    "getMemoryInfo",
                    Class.forName("android.app.ActivityManager$MemoryInfo")
            );
            Class<?> memoryInfoClass = Class.forName("android.app.ActivityManager$MemoryInfo");
            Object memoryInfo = memoryInfoClass.newInstance();
            getMemoryInfoMethod.invoke(activityManagerNative, memoryInfo);
            java.lang.reflect.Method getAvailMemMethod = memoryInfoClass.getMethod("getAvailMem");
            return (long) getAvailMemMethod.invoke(memoryInfo);
        } catch (Exception e) {
            Lg.e("MyHttpServer", "Error getting available RAM: " + e.getMessage());
            return -1;
        }
    }

}
