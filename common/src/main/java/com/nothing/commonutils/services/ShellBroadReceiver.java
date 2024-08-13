package com.nothing.commonutils.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nothing.commonutils.utils.FileUtils;
import com.nothing.commonutils.utils.Lg;

import java.io.File;


public class ShellBroadReceiver extends BroadcastReceiver {
    private static final String TAG = "ShellBroadReceiver";

    public static final String COPY_FILE = "com.nothing.common.copy_file";
    public static final String USAGE = "ShellBroadReceiver Usage:\n" +
                                       "copy_file:复制文件到指定目录\n" +
                                       String.format(
                                               "          adb shell am broadcast -a %s --es from <from_value> --es to <to_value>\n",
                                               COPY_FILE);


    @Override
    public void onReceive(Context context, Intent intent) {
        if (COPY_FILE.equals(intent.getAction())) {
            String from = intent.getStringExtra("from");
            String to = intent.getStringExtra("to");
//            adb shell am broadcast -a com.example.customaction --es from <from_value> --es to <to_value>
            boolean copyFilesTo = FileUtils.INSTANCE.copyFilesTo(new File(from), new File(to));
            Lg.i(TAG, "receiver copy file %s to %s , state:%b", from, to, copyFilesTo);
        }
    }

}
