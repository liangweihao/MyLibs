package com.nothing.commonutils.utils;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Keep;

@Keep
public class Lg {

    public static boolean isDebug = false;
    public static List<Integer> logLevel = isDebug ? Arrays.asList(Log.INFO, Log.DEBUG, Log.WARN, Log.ERROR) : Arrays.asList(Log.INFO, Log.WARN, Log.ERROR);

    public static boolean BuildStackInfo = isDebug;
    private static Context context;

    public static void d(String tag, String message, Object... args) {
        if (logLevel.contains(Log.DEBUG)) {
            String format = message + Arrays.toString(args);
            try {
                format = String.format(message, args);
            } catch (Throwable e) {

            }
            Log.d(tag, buildStack(false) + format);
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
            return Thread.currentThread().getName() + "- (" + stackTrace[2].getFileName() + ":" + stackTrace[2].getLineNumber() + ") ";
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
            Log.e(tag,  buildStack(false) + format);
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
            Log.w(tag, buildStack(false) + format);
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
            Log.i(tag, buildStack(false) + format);
            saveLogToFile(tag + "(I)", buildStack(true) + format);
        }
    }

    public static String getStackTraceAsString(Throwable throwable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        throwable.printStackTrace(ps);
        ps.close();
        return baos.toString();
    }
    


    private static final String TAG = "Lg";
    private static FileOutputStream fos = null;
    private static Handler lgHandler;
    public static File targetFile;

    public static boolean useLogcat = true;

    public static void init(Context context) {
        Lg.context = context;
        LogcatToFileUtil.INSTANCE.initLogFile(context);
        LogcatToFileUtil.INSTANCE.startLogcatCapture(context);
        if (useLogcat){
            try {
                HandlerThread handlerThread = new HandlerThread("LG");
                handlerThread.start();
                lgHandler = new Handler(handlerThread.getLooper());
                initFile(context);
                Lg.i(TAG,"Init Process PID:%s", Process.myPid());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                cleanExpiredLogs1DayBefore(context);
                cleanLogcat1DayBefore();
            }
        }).start();
    }


    public static void initFile(Context context){

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String fileName = timestamp + "_app_logs.txt";
            File logDir = new File(context.getExternalFilesDir(null).getParentFile(),"log");
            if (!logDir.exists()) {
                logDir.mkdirs();
                logDir.setExecutable(true);
                logDir.setWritable(true);
                logDir.setReadable(true);
            }
            targetFile = new File( logDir,fileName);
            if (!targetFile.exists()) {
                boolean newFile = targetFile.createNewFile();
                if (!newFile) {
                    Lg.e(TAG, "create new file false," + targetFile.getPath());
                }
            }
            fos = new FileOutputStream(targetFile);
            Lg.i(TAG, "init log file:" + targetFile.getPath());
        }catch (Throwable e){
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
        if (useLogcat){
            return;
        }
        if (lgHandler == null) {
            return;
        }
        long tid = Thread.currentThread().getId();
        lgHandler.post(() -> catchSelf(() -> {
            if (!targetFile.exists()){
                initFile(context);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
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

    // 添加清理过期日志的方法
    private static void cleanExpiredLogs1DayBefore(Context context) {
        try {

            File logDir = new File(context.getExternalFilesDir(null).getParentFile(), "log");
            if (!logDir.exists() || !logDir.isDirectory()) {
                return;
            }

            // 获取一天前的时间戳（毫秒）
            long oneWeekAgo = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000);

            File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith("_app_logs.txt"));
            if (logFiles == null) {
                return;
            }

            for (File file : logFiles) {
                // 尝试获取文件创建时间
                try {
                    Path filePath = file.toPath();
                    BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                    long creationTime = attrs.creationTime().toMillis();

                    if (creationTime < oneWeekAgo) {
                        if (file.delete()) {
                            Lg.i(TAG, "Deleted expired log file (by creation time): " + file.getName());
                        } else {
                            Lg.w(TAG, "Failed to delete expired log file (by creation time): " + file.getName());
                        }
                    }
                } catch (Exception e) {
                    // 获取创建时间失败，使用最后修改时间作为备选
                    if (file.lastModified() < oneWeekAgo) {
                        if (file.delete()) {
                            Lg.i(TAG, "Deleted expired log file (by modify time): " + file.getName());
                        } else {
                            Lg.w(TAG, "Failed to delete expired log file (by modify time): " + file.getName());
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Lg.e(TAG, "Error cleaning expired logs %s", e);
        }
    }

    private static void cleanLogcat1DayBefore() {
        try {
            // 1. 获取日志存储目录（适配工具类的路径逻辑）
            // 注意：需传入 Context，建议从 Application/Activity 获取
             File logDir = new File(LogcatToFileUtil.INSTANCE.getLogDirPath());
            if (!logDir.exists() || !logDir.isDirectory()) {
                return;
            }

            // 2. 计算一天前的时间戳（毫秒）
            long oneDayAgo = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000);

            // 3. 筛选以 logcat_ 开头的日志文件
            File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith("logcat_") && name.endsWith(".txt"));
            if (logFiles == null || logFiles.length == 0) {
                return;
            }

            // 4. 遍历文件，删除一天前的日志
            for (File logFile : logFiles) {
                try {
                String fileName = logFile.getName();
                String timeStr = fileName.replace("logcat_", "").replace(".txt", "");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
                Date fileDate = sdf.parse(timeStr);
                if (fileDate != null && fileDate.getTime() < oneDayAgo) {
                    boolean isDeleted = logFile.delete();
                    if (isDeleted) {
                        Lg.d(TAG, "Deleted expired log file by name: %s", fileName);
                    }
                }
                } catch (Exception e) {
                    // 单个文件删除失败不影响其他文件
                    Lg.e(TAG, "Error deleting log file %s: %s", logFile.getName(), e);
                }
            }
        } catch (Throwable e) {
            Lg.e(TAG, "Error cleaning expired logs %s", e);
        }
    }

}
