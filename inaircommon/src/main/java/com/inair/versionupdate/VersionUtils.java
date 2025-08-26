package com.inair.versionupdate;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VersionUtils {
   /**
    * 获取应用的版本号
    * @param context 上下文对象
    * @return 版本号，如果获取失败则返回 -1
    */
   public static int getAppVersionCode(Context context) {
      try {
         PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
         return packageInfo.versionCode;
      } catch (PackageManager.NameNotFoundException e) {
         e.printStackTrace();
      }
      return -1;
   }
}
