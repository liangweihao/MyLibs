package com.nothing.commonutils.utils;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Lg {

    public static boolean isDebug = false;
    public static List<Integer> logLevel = isDebug ? Arrays.asList(Log.INFO, Log.DEBUG, Log.WARN, Log.ERROR) : Arrays.asList(Log.INFO, Log.WARN, Log.ERROR);

    public static boolean BuildStackInfo = isDebug;

    public static void d(String tag, String message, Object... args) {
        if (logLevel.contains(Log.DEBUG)) {
            String format = message + Arrays.toString(args);
            try {
                format = String.format(message, args);
            } catch (Throwable e) {

            }
            Log.d(tag, buildStack() + format);
            saveLogToFile(tag + "(D)", buildStack(true) +  format);
        }
    }
    public static String buildStack() {
        return buildStack(false);
    }
    public static String buildStack(boolean forceBuild) {
        if (!BuildStackInfo && !forceBuild) {
            return "";
        }
        try {
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            return Thread.currentThread().getName() + "- (" + stackTrace[3].getFileName() + ":" + stackTrace[3].getLineNumber() + ") ";
        } catch (Throwable e) {
            return "";
        }
    }

    public static void e(String tag, String message, Object... args) {
        if (logLevel.contains(Log.ERROR)) {
            String format = message + Arrays.toString(args);
            try {
                format = String.format(message, args);
            } catch (Throwable e) {
            }
            Log.e(tag,  buildStack() + format);
            saveLogToFile(tag + "(E)",  buildStack(true) + format);
        }
    }

    public static void w(String tag, String message, Object... args) {
        if (logLevel.contains(Log.WARN)) {
            String format = message + Arrays.toString(args);
            try {
                format = String.format(message, args);
            } catch (Throwable e) {
            }
            Log.w(tag, buildStack() + format);
            saveLogToFile(tag + "(W)", buildStack(true) + format);
        }
    }

    public static void i(String tag, String message, Object... args) {
        if (logLevel.contains(Log.INFO)) {
            String format = message + Arrays.toString(args);
            try {
                format = String.format(message, args);
            } catch (Throwable e) {
            }
            Log.i(tag, buildStack() + format);
            saveLogToFile(tag + "(I)", buildStack(true) + format);
        }
    }


    private static final String TAG = "Lg";
    private static FileOutputStream fos = null;
    private static Handler lgHandler;

    public static void init(Context context) {
        try {
            HandlerThread handlerThread = new HandlerThread("LG");
            handlerThread.start();
            lgHandler = new Handler(handlerThread.getLooper());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            String fileName = timestamp + "_app_logs.txt";
            File file = new File(context.getExternalCacheDir(), fileName);
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                if (!newFile) {
                    Lg.e(TAG, "create new file false," + file.getPath());
                }
            }
            fos = new FileOutputStream(file);
            Lg.i(TAG, "init log file:" + file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Throwable catchSelf(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            t.printStackTrace();
            return t;
        }
        return null;
    }


    public static void saveLogToFile(String tag, String logMessage) {
        if (lgHandler == null) {
            return;
        }
        long tid = Thread.currentThread().getId();
        lgHandler.post(() -> catchSelf(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String log = timestamp + " - " + tid + ":" + tag + " :  " + logMessage + "\n";
            try {
                if (fos != null) {
                    fos.write(log.getBytes());
                    fos.flush();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }));

    }

}
