package com.inair.inaircommon;


import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings;

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

}
