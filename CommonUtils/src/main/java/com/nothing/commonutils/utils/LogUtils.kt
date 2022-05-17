package com.nothing.commonutils.utils

import com.orhanobut.logger.*

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

fun printer(printer:Printer) {
    Logger.printer(printer)
}

fun addLogAdapter(adapter:LogAdapter) {
    Logger.addLogAdapter(adapter)
}

fun clearLogAdapters() {
    Logger.clearLogAdapters()
}

fun t(tag:String?):Printer? {
    return Logger.t(tag)
}

/**
 * General log function that accepts all configurations as parameter
 */
fun log(priority:Int, tag:String?, message:String?, throwable:Throwable?) {
    Logger.log(priority, tag, message, throwable)
}

fun d(message:String, vararg args:Any?) {
    Logger.d(message, *args)
}

fun d(`object`:Any?) {
    Logger.d(`object`)
}

fun e(message:String, vararg args:Any?) {
    Logger.e(null, message, *args)
}

fun e(throwable:Throwable?, message:String, vararg args:Any?) {
    Logger.e(throwable, message, *args)
}

fun i(message:String, vararg args:Any?) {
    Logger.i(message, *args)
}

fun v(message:String, vararg args:Any?) {
    Logger.v(message, *args)
}

fun w(message:String, vararg args:Any?) {
    Logger.w(message, *args)
}


fun wtf(message:String, vararg args:Any?) {
    Logger.wtf(message, *args)
}


fun json(json:String?) {
    Logger.json(json)
}

fun xml(xml:String?) {
    Logger.xml(xml)
}