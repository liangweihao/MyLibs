package com.nothing.commonutils.utils;


import androidx.annotation.Nullable;

public class Try {

    @Nullable
    public static Throwable catchSelf(Runnable runnable){
        try{
            runnable.run();
        }catch (Throwable t){
            t.printStackTrace();
            return t;
        }
        return null;
    }
}
