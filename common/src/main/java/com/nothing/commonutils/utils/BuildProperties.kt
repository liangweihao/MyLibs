package com.nothing.commonutils.utils

import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

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

class BuildProperties private constructor() {
    private val properties:Properties = Properties()
    fun containsKey(key:Any):Boolean {
        return properties.containsKey(key)
    }

    fun containsValue(value:Any):Boolean {
        return properties.containsValue(value)
    }

    fun entrySet():Set<Map.Entry<Any, Any>> {
        return properties.entries
    }

    fun getProperty(name:String?):String {
        return properties.getProperty(name)
    }

    fun getProperty(name:String?, defaultValue:String?):String {
        return properties.getProperty(name, defaultValue)
    }

    val isEmpty:Boolean
        get() = properties.isEmpty

    fun keys():Enumeration<Any> {
        return properties.keys()
    }

    fun keySet():Set<Any> {
        return properties.keys
    }

    fun size():Int {
        return properties.size
    }

    fun values():Collection<Any> {
        return properties.values
    }

    companion object {
        @Throws(IOException::class) fun newInstance():BuildProperties {
            return BuildProperties()
        }
    }

    init {
        properties.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
    }
}