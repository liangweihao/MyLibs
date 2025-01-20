package com.nothing.commonutils.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * --------------------
 * <p>Author：
 * lwh
 * <p>Created Time:
 * 2025/1/18
 * <p>Intro:
 *
 * <p>Thinking:
 *
 * <p>Problem:
 *
 * <p>Attention:
 * --------------------
 */

public class CheckAppRuning {

    private static final String TAG = "CheckAppRuning";
    public List<String> exitPackageName = new ArrayList<>();
    BroadcastReceiver exitCallbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String aPackage = intent.getStringExtra("packageName");
            if (!TextUtils.isEmpty(aPackage)) {
                Lg.i(TAG, "check_app_exit_callback : %s", aPackage);
                exitPackageName.add(aPackage);
                Lg.i(TAG,
                        "check_app_exit_callback onReceive : %s",
                        Arrays.toString(exitPackageName.toArray(new String[0]))
                );
            } else {
                Lg.w(TAG, "Package Callback Name is Empty");
            }

        }
    };

    public void registerExitCallbackReceiver(Context context) {
        context.registerReceiver(exitCallbackReceiver,
                new IntentFilter("check_app_exit_callback"),
                0
        );
    }

    private BroadcastReceiver exitReceiver;

    public void registerExitPackageRecevier(
            Context context, String packageName, BroadcastReceiver receiver
    ) {

        exitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String aPackage = intent.getPackage();
                if (!TextUtils.isEmpty(aPackage) && Objects.equals(packageName, aPackage)) {
                    Lg.i(TAG, "check_app_exit : %s", aPackage);
                    sendExitCallbackBroadcast(context, aPackage);
                    receiver.onReceive(context, intent);
                } else {
                    Lg.w(TAG, "Package Name is Empty");
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter("check_app_exit");
        try {
            intentFilter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        context.registerReceiver(exitReceiver,intentFilter, 0);
    }

    public void unregisterExitPackageRecevier(Context context) {
        if (exitReceiver != null) {
            context.unregisterReceiver(exitReceiver);
        }
        Lg.i(TAG,
                "unregisterExitPackageReceiver() called with: context = [" + context + "], receiver = [" + exitReceiver + "]"
        );
    }

    public void unregisterExitCallbackPackageRecevier(Context context) {
        context.unregisterReceiver(exitCallbackReceiver);
        Lg.i(TAG,
                "unregisterExitCallbackPackageReceiver() called with: context = [" + context + "], receiver = [" + exitCallbackReceiver + "]"
        );
    }

    public void sendExitCallbackBroadcast(Context context, String packageName) {
        Intent checkIntent = new Intent("check_app_exit_callback");
        checkIntent.putExtra("packageName",packageName);
        context.sendBroadcast(checkIntent);
    }

    public void sendCheckBroadcast(
            Context context, String packageName, Bundle bundle, Uri uri, String mimeType
    ) {
        exitPackageName.clear();
        try {
            Intent checkIntent = new Intent("check_app_exit");
            checkIntent.setPackage(packageName);
            checkIntent.setDataAndType(uri, mimeType);
            checkIntent.putExtras(bundle);
            checkIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(checkIntent);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

}
