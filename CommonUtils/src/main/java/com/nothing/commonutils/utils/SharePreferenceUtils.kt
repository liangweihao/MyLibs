package com.nothing.commonutils.utils

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/6/2
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
// 通过文件获得sp
fun getSharePreference(context:Context, dir:File):SharedPreferences {
    val name = dir.name.replace(".xml", "")
    return context.getSharedPreferences(name, Context.MODE_PRIVATE)
}

fun getSharePreference(context:Context, name:String):SharedPreferences {
    return context.getSharedPreferences(name, Context.MODE_PRIVATE)
}

// 读取sp的属性
fun getSharePreferceContent(dir:File):Observable<HashMap<String, XmlValue>> {
    return Observable.create<HashMap<String, XmlValue>> {
        val readXmlFromFile = readXmlFromFile(dir)
        it.onNext(readXmlFromFile)
        it.onComplete()
    }.subscribeOn(Schedulers.io())
}

