package com.inair.versionupdate;

import android.content.Context;
import android.content.SharedPreferences;

import com.nothing.commonutils.utils.Lg;

public class UpdateGuideManager {
   private static final String PREF_NAME = "UpdateGuidePrefs";
   private static final String KEY_LAST_SHOWN_VERSION = "last_shown_version";

   private static final String TAG = "UpdateGuideManager";
   /**
    * 判断是否需要显示升级引导
    * @param context 上下文对象
    * @return 如果需要显示则返回 true，否则返回 false
    */
   public static boolean shouldShowUpdateGuide(Context context) {
      int currentVersion = VersionUtils.getAppVersionCode(context);
      // 记录获取到的当前版本号
      Lg.i(TAG, "Current app version code: " + currentVersion);
      if (currentVersion == -1) {
         // 记录获取版本号失败的日志
         Lg.e(TAG, "Failed to get app version code, returning false");
         return false;
      }

      SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
      int lastShownVersion = prefs.getInt(KEY_LAST_SHOWN_VERSION, -1);
      // 记录上次显示引导的版本号
      Lg.i(TAG, "Last shown version code: " + lastShownVersion);

      if (currentVersion > lastShownVersion) {
         // 记录需要显示引导并更新版本号的日志
         Lg.i(TAG, "Current version is newer than last shown version. Updating last shown version and returning true");
         // 更新存储的版本号
         prefs.edit().putInt(KEY_LAST_SHOWN_VERSION, currentVersion).apply();
         return true;
      }
      // 记录不需要显示引导的日志
      Lg.i(TAG, "Current version is not newer than last shown version. Returning false");
      return false;
   }
}
