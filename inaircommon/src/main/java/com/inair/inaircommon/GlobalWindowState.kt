package com.inair.inaircommon

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nothing.commonutils.utils.FragmentUtils
import com.nothing.commonutils.utils.Lg
import kotlin.math.abs


object GlobalWindowState : Application.ActivityLifecycleCallbacks {
    private var newConfig: Configuration? = null
    enum class WindowSizeState{
        NONE,
        FIRST_LAYOUT,
        MIN,
        MAX,
        CLOSE
    }
    private const val TAG = "GlobalWindowState"

    private val _globalWindowState: MutableLiveData<WindowSizeState> = MutableLiveData<WindowSizeState>(WindowSizeState.NONE)

    private val activityStateMap = HashMap<Activity,MutableLiveData<WindowSizeState>>()
    fun isFullWindow(): Boolean {
        if (newConfig == null) {
            return false
        }
        return isFullMode(newConfig!!)
    }

    fun isFullMode(newConfig: Configuration): Boolean {
        return 1080 == GlobalWindowState.newConfig!!.screenHeightDp
    }

    fun getWindowState(activity: Activity? = null):MutableLiveData<WindowSizeState>{
        if (activity == null){
            return _globalWindowState
        }
        val mutableLiveData = activityStateMap[activity]
        if(mutableLiveData == null){
            activityStateMap[activity] = MutableLiveData()
        }
        return  activityStateMap[activity]!!
    }


    fun updateWindowState(activity: Activity,state: WindowSizeState) {
        _globalWindowState.postValue(state)
        activityStateMap[activity]?.postValue(state)
    }

    private fun initConfig(configuration: Configuration) {
        newConfig = configuration
    }


//    private var windowState = WindowSizeState.NONE

    private var layoutMap = HashMap<Activity,OnLayoutChangeListener>()
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        initConfig(activity.resources.configuration)
        (activity as? FragmentActivity)?.also {
            FragmentUtils.addFragment(it.supportFragmentManager,it,android.R.id.content,ConfigFragment(),"")
        }

//        activityStateMap[activity] = MutableLiveData()
//        windowState = WindowSizeState.NONE
        activity.window.decorView.addOnLayoutChangeListener(OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (oldLeft == 0 && oldRight == 0 && oldTop == 0 && oldBottom == 0
                && (abs(right - left) > 0 && abs(bottom - top) > 0)
                && getWindowState(activity).value != WindowSizeState.FIRST_LAYOUT
            ) {
                getWindowState(activity).postValue(WindowSizeState.FIRST_LAYOUT)
                Lg.d(TAG, "first layout complete")
                dispatchWindowState(activity,left, right, top, bottom)
            } else {
                dispatchWindowState(activity,left, right, top, bottom)
            }
        }.also {
            layoutMap[activity] = it
        })
    }

    private fun dispatchWindowState(activity: Activity,left: Int, right: Int, top: Int, bottom: Int) {
        if (isFullWindow() && (  1080 == newConfig!!.screenHeightDp) && getWindowState(activity).value != WindowSizeState.MAX
        ) {
            updateWindowState(activity, WindowSizeState.MAX)
            Lg.d(TAG, "enter max window")
        } else if (!isFullWindow() && ( newConfig!!.screenHeightDp == 800) && getWindowState(activity).value != WindowSizeState.MIN
        ) {
            updateWindowState(activity, WindowSizeState.MIN)
            Lg.d(TAG, "enter min window")
        }
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        layoutMap.remove(activity)
        activityStateMap.remove(activity)
        updateWindowState(activity, WindowSizeState.CLOSE)

    }

    internal class ConfigFragment : Fragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            initConfig(resources.configuration)

            newConfig?.also {
                updateWindowState(requireActivity(),if(isFullMode(it)) WindowSizeState.MAX else WindowSizeState.MIN)
            }
        }

        override fun onConfigurationChanged(config: Configuration) {
            super.onConfigurationChanged(config)
            newConfig = config
            updateWindowState(requireActivity(),if(isFullMode(config)) WindowSizeState.MAX else WindowSizeState.MIN)
        }
    }
}