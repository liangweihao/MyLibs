package com.nothing.commonutils.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.nothing.commonutils.utils.BugReporterZip;
import com.nothing.commonutils.utils.FileUtils;
import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.NewFileUtils;
import com.nothing.commonutils.utils.RefInvoke;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class ShellBroadReceiver extends BroadcastReceiver {
    private static final String TAG = "ShellBroadReceiver";

    public static final String COPY_FILE = "copy_file";
    public static final String FILE_LIST = "file_list";
    public static final String REPORT = "report";
    public static final String STATIC_CLASS = "static_class";


    private String USAGE = "";

    private String baseTag = "";

    public ShellBroadReceiver(String baseTag) {
        this.baseTag = baseTag;
        USAGE = "ShellBroadReceiver Usage:\n" + "copy_file:复制文件到指定目录\n" + String.format(
                "adb shell am broadcast -a %s --es from _ --es to _\n",
                appendAction(COPY_FILE)
        ) + String.format(
                "adb shell am broadcast -a %s  --es file _\n",
                appendAction(FILE_LIST)
        ) + String.format("adb shell am broadcast -a %s \n", appendAction(REPORT))
                + String.format(
                "adb shell am broadcast -a %s --es class _ --es field _ \n",
                appendAction(STATIC_CLASS)
        ) +
                String.format(
                        "adb shell am broadcast -a %s --es class _ --es field _ --es method _ --es args _\n",
                        appendAction(STATIC_CLASS)
                )

        ;
        Lg.i(TAG, USAGE);
    }

    public IntentFilter getIntentFileter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(appendAction(COPY_FILE));
        intentFilter.addAction(appendAction(FILE_LIST));
        intentFilter.addAction(appendAction(REPORT));
        intentFilter.addAction(appendAction(STATIC_CLASS));
        return intentFilter;
    }

    private String appendAction(String subAction) {
        return baseTag + "." + subAction;
    }


    private boolean equalAction(String subAction, String inputAction) {
        if ((baseTag + "." + subAction).equals(inputAction)) {
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (equalAction(COPY_FILE, intent.getAction())) {
            String from = intent.getStringExtra("from");
            String to = intent.getStringExtra("to");
//            adb shell am broadcast -a com.example.customaction --es from <from_value> --es to <to_value>
            boolean copyFilesTo = false;
            if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(to)) {
                copyFilesTo = FileUtils.INSTANCE.copyFilesTo(new File(from), new File(to));
            }
            Lg.i(TAG, "receiver copy file %s to %s , state:%b", from, to, copyFilesTo);
        } else if (equalAction(FILE_LIST, intent.getAction())) {
            String from = intent.getStringExtra("file");
            if (!TextUtils.isEmpty(from)) {
                File file = new File(from);
                File[] files = file.listFiles();
                String string = Arrays.toString(files);
                Lg.i(TAG, "get file %s list :%s", files, string);
            }
        } else if (equalAction(REPORT, intent.getAction())) {
            File file1 = new File(context.getExternalFilesDir(null), "log");
            File file2 = new File(context.getExternalFilesDir(null), "crash");
            BugReporterZip.zipLogFiles(context, new File[]{file1, file2});
            try {
                NewFileUtils.deleteDirectory(file1);
                NewFileUtils.deleteDirectory(file2);
                Lg.i(TAG, "Report Suc %s", file1.getPath());
                Lg.i(TAG, "Report Suc %s", file2.getPath());
            } catch (IOException e) {
                e.fillInStackTrace();
            }

        } else if (equalAction(STATIC_CLASS, intent.getAction())) {
            String aClass = intent.getStringExtra("class");
            String field = intent.getStringExtra("field");
            String method = intent.getStringExtra("method");
            String[] args = new String[]{};
            try {
                args = intent.getStringArrayExtra("args");
                if (args == null){
                    args = new String[]{intent.getStringExtra("args")};
                }
            }catch (Throwable e){
                e.fillInStackTrace();
                args = new String[]{intent.getStringExtra("args")};
            }
            Object staticFieldObject = RefInvoke.getStaticFieldObject(aClass, field);
            if (TextUtils.isEmpty(method)) {
                Lg.i(
                        TAG,
                        "Invoke Class:%s Field:%s \n Result:%s",
                        aClass,
                        field,
                        String.valueOf(staticFieldObject)
                );
            } else {

                Class<String>[] classes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    classes[i] = String.class;
                }
                Lg.i(
                        TAG,
                        "Invoke Class:%s Field:%s Method:%s \n Result:%s",
                        aClass,
                        field,
                        method,
                        RefInvoke.invokeInstanceMethod(staticFieldObject, method, classes, args)
                );
            }
        }
    }

}
