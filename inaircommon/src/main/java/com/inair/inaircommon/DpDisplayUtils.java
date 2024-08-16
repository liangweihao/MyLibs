package com.inair.inaircommon;


import android.content.Context;
import android.view.Display;

public class DpDisplayUtils {

    // true  虚拟屏覆盖了应用
    public static boolean isVirtualSreenOn(Context context){
        Display display = context.getDisplay();
        if (display == null){
            return false;
        }
        return display.getState() == Display.STATE_ON;
    }
}
