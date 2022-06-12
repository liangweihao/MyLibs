package com.nothing.commonutils.services

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LiveData
import com.nothing.commonutils.utils.SharePreferenceService
import com.nothing.commonutils.utils.d
import java.io.File

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/19
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */

fun Context.bindServices(connection:CommonBackgroundConnection) {

    bindService(Intent(this, CommonBackgroundServer::class.java), connection, Context.BIND_AUTO_CREATE)
}

abstract class CommonBackgroundConnection:ServiceConnection {
    final override fun onServiceConnected(name:ComponentName?, service:IBinder?) {
        onServiceConnected(service as CommonBackgroundServer.CommonBackgroundBinder)
    }

    abstract fun onServiceConnected(binder:CommonBackgroundServer.CommonBackgroundBinder)

    override fun onServiceDisconnected(name:ComponentName?) {
    }
}

class CommonBackgroundServer:Service() {
    private val TAG = "CommonBackgroundServer"
    private var sharePreferenceService:SharePreferenceService? = null

    private var binder = CommonBackgroundBinder()
    override fun onBind(intent:Intent?):CommonBackgroundBinder {
        return binder
    }

    override fun onStartCommand(intent:Intent?, flags:Int, startId:Int):Int {
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        super.onDestroy()
        sharePreferenceService?.unListener()
        sharePreferenceService = null

    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onCreate() {
        d("$TAG onCreate")
        sharePreferenceService = SharePreferenceService()
        sharePreferenceService?.scanXml(baseContext!!)
        sharePreferenceService?.listener(baseContext!!)
        super.onCreate()
    }

    override fun onConfigurationChanged(newConfig:Configuration) {
        super.onConfigurationChanged(newConfig)
    }


    public inner class CommonBackgroundBinder:Binder() {
        fun getShareFileList():LiveData<List<File>> {
            return sharePreferenceService!!.shareFileResult
        }
    }


}