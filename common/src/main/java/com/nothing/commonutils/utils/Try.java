package com.nothing.commonutils.utils;


import java.util.concurrent.Callable;

import androidx.annotation.Nullable;

public class Try {

    @Nullable
    public static Throwable catchSelf(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            t.printStackTrace();
            return t;
        }
        return null;
    }

    @Nullable
    public static void catchSelfNoThrowable(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e){


        }
    }


    public static Object useOrNull(Callable<?> callable){
        try {
            return callable.call();
        } catch (Exception e) {
            return null;
        }
    }
}
