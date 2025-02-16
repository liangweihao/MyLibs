package com.inair.inaircommon;


import android.content.Context;
import android.os.Vibrator;
import android.view.View;

import com.nothing.commonutils.utils.RefInvoke;

/**
 * xml 加震动  hover_vibrator 或者 View.setHoverListener
 *
 *
 * */
public class VibratorUtils {

    public static void performTickClick(View object) {
        RefInvoke.invokeInstanceMethod(object,
                                       View.class, "playPreDefinedVibrator",
                                       new Class[]{RefInvoke.getClass("android.os.Vibrator$PreDefineType")},
                                       new Object[]{RefInvoke.getEnum(RefInvoke.getClass("android.os.Vibrator$PreDefineType"), "TICK_200")});
    }
    public static void performTickHover(View object) {
        RefInvoke.invokeInstanceMethod(object,
                View.class, "playPreDefinedVibrator",
                new Class[]{RefInvoke.getClass("android.os.Vibrator$PreDefineType")},
                new Object[]{RefInvoke.getEnum(RefInvoke.getClass("android.os.Vibrator$PreDefineType"), "CLICK")});
    }


    public static void performTickClick(Context context){
        Vibrator vibrator = context.getSystemService(Vibrator.class);
        RefInvoke.invokeInstanceMethod(vibrator,"playPreDefineType",new Class[]{RefInvoke.getClass("android.os.Vibrator$PreDefineType")},
                                       new Object[]{RefInvoke.getEnum(RefInvoke.getClass("android.os.Vibrator$PreDefineType"), "TICK_200")});

    }

    public static void performTickHover(Context context){
        Vibrator vibrator = context.getSystemService(Vibrator.class);
        RefInvoke.invokeInstanceMethod(vibrator,"playPreDefineType",new Class[]{RefInvoke.getClass("android.os.Vibrator$PreDefineType")},
                                       new Object[]{RefInvoke.getEnum(RefInvoke.getClass("android.os.Vibrator$PreDefineType"), "CLICK")});

    }

}
