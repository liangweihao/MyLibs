package com.nothing.commonutils.services

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nothing.commonutils.utils.d
import java.io.File
import java.io.FileDescriptor

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
class CommonBackgroundServer:Service() {
    private val TAG = "CommonBackgroundServer"
    private var sharePreferenceService: SharePreferenceService? = null

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