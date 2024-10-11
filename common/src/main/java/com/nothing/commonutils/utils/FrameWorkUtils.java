package com.nothing.commonutils.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import com.nothing.commonutils.lifecycle.LifeContext;

import androidx.lifecycle.Lifecycle;

public class FrameWorkUtils {


    public static boolean autoBindService(
            Lifecycle lifecycle, Context context, Intent intent, ServiceConnection conn, int flags
    ) {
        boolean bindService = context.bindService(intent, conn, flags);
        LifeContext.INSTANCE.doOnDestory(conn, lifecycle, serviceConnection -> {
            Try.catchSelf(() -> context.unbindService(conn));
            return true;
        });
        return bindService;
    }


    public static Intent autoRegisterBroadcast(
            Lifecycle lifecycle, Context context, BroadcastReceiver receiver, IntentFilter filter,
            int flags
    ) {

        Intent intent = context.registerReceiver(receiver, filter, flags);
        LifeContext.INSTANCE.doOnDestory(receiver, lifecycle, r -> {
            Try.catchSelf(() -> context.unregisterReceiver(r));
            return true;
        });
        return intent;
    }

}
