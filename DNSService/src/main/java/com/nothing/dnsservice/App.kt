package com.nothing.dnsservice

import android.app.Application
import android.content.Context

/**
 *--------------------
 *<p>Author：
 *         lwh
 *<p>Created Time:
 *          2025/7/25
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
class App : Application() {


    companion object {
        lateinit var context: Context
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = this
    }
}