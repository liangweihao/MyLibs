package com.inair.inaircommon;


import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;

import com.inair.ref.RefInvoke;

public class SettingGlobalManager {

    public static boolean isArSpaceEnable(ContentResolver contentResolver) {
        return Settings.Global.getInt(
                contentResolver,
                "ar_space_enable", 1
        ) == 1;
    }


    public static void registerAirSpaceChangeListener(
            ContentResolver contentResolver,
            ContentObserver observer
    ) {
        Uri arSpaceUri = Settings.Global.getUriFor("ar_space_enable");
        contentResolver.registerContentObserver(
                arSpaceUri, false, observer);
    }

    public static void unregisterContentObserver(
            ContentResolver contentResolver,
            ContentObserver observer
    ) {
        contentResolver.unregisterContentObserver(observer);
    }


    // 或者反射 android.os.Build.IS_GMS_DEVICE Boolean
    public static boolean isGMS(Context context) {
        Object activityTaskManagerInstance = RefInvoke.invokeStaticMethod(
                "android.app.ActivityTaskManager",
                "getInstance",
                null,
                null
        );
        if (activityTaskManagerInstance != null) {
            // 反射调用 isGmsDevice 方法
            Object result = RefInvoke.invokeInstanceMethod(
                    activityTaskManagerInstance,
                    "isGmsDevice",
                    null,
                    null
            );

            if (result instanceof Boolean) {
                if ((Boolean) result) {
                    return true;
                }
            }
        }
        Object fieldObject = RefInvoke.getFieldObject("android.os.Build", null, "IS_GMS_DEVICE");
        if (fieldObject != null && fieldObject instanceof Boolean) {
            if (((Boolean) fieldObject)) {
                return true;
            }
        }
        return Settings.System.getInt(
                context.getContentResolver(),
                "dp_is_gms_patch", // Settings.System.IS_GMS_PATCH 的字符串值
                0 // 默认值为 0（非 GMS）
        ) == 1;
    }
}
