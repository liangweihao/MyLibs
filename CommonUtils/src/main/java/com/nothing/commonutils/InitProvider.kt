package com.nothing.commonutils

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import com.nothing.commonutils.services.CommonBackgroundServer
import com.nothing.commonutils.services.SharePreferenceService
import com.nothing.commonutils.utils.addLogAdapter
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.CsvFormatStrategy
import com.orhanobut.logger.PrettyFormatStrategy

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/17
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
class InitProvider:ContentProvider() {
    override fun onCreate():Boolean {
        addLogAdapter(AndroidLogAdapter(PrettyFormatStrategy.newBuilder().methodCount(1)
                                            .showThreadInfo(false).methodOffset(1)
                                            .tag("CommonLog").build()))
        addLogAdapter(AndroidLogAdapter(CsvFormatStrategy.newBuilder().build()))
        context!!.startService(Intent(context, CommonBackgroundServer::class.java))
        return true
    }

    override fun query(uri:Uri, projection:Array<out String>?, selection:String?, selectionArgs:Array<out String>?, sortOrder:String?):Cursor? {
        return null
    }

    override fun getType(uri:Uri):String? {
        return null
    }

    override fun insert(uri:Uri, values:ContentValues?):Uri? {
        return null
    }

    override fun delete(uri:Uri, selection:String?, selectionArgs:Array<out String>?):Int {
        return 0
    }

    override fun update(uri:Uri, values:ContentValues?, selection:String?, selectionArgs:Array<out String>?):Int {
        return 0
    }

}