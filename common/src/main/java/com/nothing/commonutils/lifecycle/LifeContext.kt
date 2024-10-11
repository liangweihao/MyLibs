package com.nothing.commonutils.lifecycle

import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.nothing.commonutils.utils.thread.WorkThreadLoader


object LifeContext {

    /**
     *
     * @param scope return true 自动移除这个监听
     * */
    fun <T> T.doOnDestory(lifecycle: Lifecycle,scope:(T)->Boolean): T {
        var r = {
            lifecycle.addObserver(object : LifecycleEventObserver{
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY){
                        if(scope.invoke(this@doOnDestory)){
                            lifecycle.removeObserver(this)
                        }
                    }
                }
            })
        }
        if (Looper.myLooper() == Looper.getMainLooper()){
            r.invoke()
        }else {
            WorkThreadLoader.createMainTask(Runnable {
                r.invoke()
            })
        }
        return this
    }
    fun<T> T.doOnResume(lifecycle: Lifecycle,scope:(T)->Boolean): T {
        var r = {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (scope.invoke(this@doOnResume)) {
                            lifecycle.removeObserver(this)
                        }
                    }
                }
            })
        }
        if (Looper.myLooper() == Looper.getMainLooper()){
            r.invoke()
        }else {
            WorkThreadLoader.createMainTask(Runnable {
                r.invoke()
            })
        }
        return this
    }

    fun<T> T.doOnPause(lifecycle: Lifecycle,scope:(T)->Boolean): T {
        var r = {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        if (scope.invoke(this@doOnPause)) {
                            lifecycle.removeObserver(this)
                        }
                    }
                }
            })
        }
        if (Looper.myLooper() == Looper.getMainLooper()){
            r.invoke()
        }else {
            WorkThreadLoader.createMainTask(Runnable {
                r.invoke()
            })
        }
        return this
    }


}