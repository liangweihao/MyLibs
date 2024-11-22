package com.nothing.commonutils

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.nothing.commonutils.services.ShellBroadReceiver
import com.nothing.commonutils.utils.ApplicationLivecycleListener
import com.nothing.commonutils.utils.ApplicationMessagePrinter
import com.nothing.commonutils.utils.DisplayUtils
import com.nothing.commonutils.utils.Lg
import com.nothing.commonutils.utils.LocalCrashHandler
import com.nothing.commonutils.utils.Try

/**
 *
 * android {
 *     // 其他已有的配置...
 *
 *     defaultConfig {
 *         // 其他已有的默认配置...
 *
 *         // 定义一个用于存储打包时间的变量
 *         def buildTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
 *
 *         // 向manifest中添加meta-data元素来存储打包时间
 *         manifestPlaceholders = [
 *                 buildTime: buildTime
 *         ]
 *     }
 *
 *     // 其他已有的配置...
 * }
 *
 *   <meta-data
 *         android:name="BUILD_TIME"
 *         android:value="${buildTime}" />
 * */
class InitProvider : ContentProvider() {
    private val TAG = "InitProvider"

    override fun onCreate(): Boolean {
        Lg.init(context)
        Try.catchSelf {
            val applicationInfo = context!!.packageManager.getApplicationInfo(context!!.packageName, PackageManager.GET_META_DATA)
            val buildTime = applicationInfo.metaData.getString("buildTime", "")
            if (!TextUtils.isEmpty(buildTime)){
                Lg.i(TAG,"ApkBuildTime:${buildTime}")
            }
            val packageInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            Lg.i(
                TAG,
                "VersionName:${packageInfo.versionName} VersionCode:${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode}"
            )
        }
        Thread.setDefaultUncaughtExceptionHandler(LocalCrashHandler(context!!))
        val applicationLivecycleListener = ApplicationLivecycleListener()
        context!!.registerComponentCallbacks(applicationLivecycleListener)
        (context!!.applicationContext as? Application)?.registerActivityLifecycleCallbacks(
            applicationLivecycleListener
        )
        Looper.getMainLooper().setMessageLogging(ApplicationMessagePrinter())
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

}