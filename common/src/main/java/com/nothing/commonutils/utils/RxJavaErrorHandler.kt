package com.nothing.commonutils.utils

import com.nothing.commonutils.utils.Lg
import com.nothing.commonutils.utils.Lg.getStackTraceAsString
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins

object RxJavaErrorHandler {
    private const val TAG = "RxJavaErrorHandler"
    
    fun setup() {
        RxJavaPlugins.setErrorHandler { throwable: Throwable? ->
            var throwable = throwable
            if (throwable is OnErrorNotImplementedException) {
                throwable = throwable.cause
            }
            if (throwable is UndeliverableException) {
                throwable = throwable.cause
            }
            if (throwable != null) {
                val stackTraceAsString: String = getStackTraceAsString(throwable)
                Lg.e(TAG, "Rxjava Error Stack : %s", stackTraceAsString)
            }
        }
    }
}