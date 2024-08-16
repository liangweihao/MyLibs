package com.nothing.commonutils

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.nothing.commonutils.services.ShellBroadReceiver
import com.nothing.commonutils.utils.ApplicationLivecycleListener
import com.nothing.commonutils.utils.ApplicationMessagePrinter
import com.nothing.commonutils.utils.Lg
import com.nothing.commonutils.utils.LocalCrashHandler


class InitProvider : ContentProvider() {
    private  val TAG = "InitProvider"

    override fun onCreate(): Boolean {
        Lg.init(context)
        Thread.setDefaultUncaughtExceptionHandler(LocalCrashHandler(context!!))
        Lg.d("Shell", ShellBroadReceiver.USAGE)
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