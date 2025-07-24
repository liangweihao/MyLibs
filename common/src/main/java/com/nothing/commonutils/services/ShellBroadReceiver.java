package com.nothing.commonutils.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.inair.ref.RefInvoke;
import com.nothing.commonutils.utils.BugReporterZip;
import com.nothing.commonutils.utils.DynamicClassLoader;
import com.nothing.commonutils.utils.Lg;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ShellBroadReceiver extends BroadcastReceiver {
    private static final String TAG = "ShellBroadReceiver";

    public static final String COPY_FILE = "copy_file";
    public static final String FILE_LIST = "file_list";
    public static final String REPORT = "report";
    public static final String STATIC_CLASS = "static_class";
    public static final String DYN_CLASS = "dyn_class";
    // 新增调试命令常量
    public static final String CLEAR_DIR = "clear_dir";
    public static final String PRINT_PROPERTY = "print_property";
    public static final String EXECUTE_COMMAND = "execute_command";

    private String USAGE = "";
    private String baseTag = "";

    public ShellBroadReceiver(String baseTag) {
        this.baseTag = baseTag;
        USAGE = "ShellBroadReceiver Usage:\n" +
                "copy_file:复制文件到指定目录\n" +
                String.format("adb shell am broadcast -a %s --es from _ --es to _\n", appendAction(COPY_FILE)) +
                String.format("adb shell am broadcast -a %s  --es file _\n", appendAction(FILE_LIST)) +
                String.format("adb shell am broadcast -a %s \n", appendAction(REPORT)) +
                String.format("adb shell am broadcast -a %s --es class _ --es field _ \n", appendAction(STATIC_CLASS)) +
                String.format("adb shell am broadcast -a %s --es class _ --es field _ --es method _ --es args _\n", appendAction(STATIC_CLASS)) +
                String.format("adb shell am broadcast -a %s --es path _\n", appendAction(DYN_CLASS)) +
                // 新增调试命令使用说明
                String.format("adb shell am broadcast -a %s --es dir _\n", appendAction(CLEAR_DIR)) +
                String.format("adb shell am broadcast -a %s --es key _\n", appendAction(PRINT_PROPERTY)) +
                String.format("adb shell am broadcast -a %s --es command _\n", appendAction(EXECUTE_COMMAND));

        Lg.i(TAG, USAGE);
    }

    public IntentFilter getIntentFileter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(appendAction(COPY_FILE));
        intentFilter.addAction(appendAction(FILE_LIST));
        intentFilter.addAction(appendAction(REPORT));
        intentFilter.addAction(appendAction(STATIC_CLASS));
        intentFilter.addAction(appendAction(DYN_CLASS));
        // 新增调试命令的 IntentFilter
        intentFilter.addAction(appendAction(CLEAR_DIR));
        intentFilter.addAction(appendAction(PRINT_PROPERTY));
        intentFilter.addAction(appendAction(EXECUTE_COMMAND));
        return intentFilter;
    }

    private String appendAction(String subAction) {
        return baseTag + "." + subAction;
    }

    private boolean equalAction(String subAction, String inputAction) {
        return (baseTag + "." + subAction).equals(inputAction);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (equalAction(COPY_FILE, intent.getAction())) {
            String from = intent.getStringExtra("from");
            String to = intent.getStringExtra("to");
            boolean copySuccess = false;

            if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(to)) {
                try {
                    java.nio.file.Path sourcePath = java.nio.file.Paths.get(from);
                    java.nio.file.Path targetPath = java.nio.file.Paths.get(to);

                    // 检查源文件是否存在
                    if (java.nio.file.Files.exists(sourcePath)) {
                        // 复制文件或目录
                        if (java.nio.file.Files.isDirectory(sourcePath)) {
                            copyDirectory(sourcePath, targetPath);
                        } else {
                            java.nio.file.Files.copy(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                        copySuccess = true;
                    } else {
                        Lg.e(TAG, "Source file or directory does not exist: %s", from);
                    }
                } catch (java.io.IOException e) {
                    Lg.e(TAG, "Failed to copy file from %s to %s: %s", from, to, e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Lg.e(TAG, "Source or target path is empty. from: %s, to: %s", from, to);
            }

            Lg.i(TAG, "receiver copy file %s to %s , state:%b", from, to, copySuccess);
        } else if (equalAction(FILE_LIST, intent.getAction())) {
            String from = intent.getStringExtra("file");
            if (!TextUtils.isEmpty(from)) {
                File file = new File(from);
                File[] files = file.listFiles();
                String string = Arrays.toString(files);
                Lg.i(TAG, "get file %s list :%s", from, string);
            }
        } else if (equalAction(REPORT, intent.getAction())) {
            File file1 = new File(context.getExternalFilesDir(null).getParentFile(), "log");
            File file2 = new File(context.getExternalFilesDir(null).getParentFile(), "crash");
            BugReporterZip.zipLogFiles(context, new File[]{file1, file2});
            try {
                deleteFilesInDirectory(file1);
                deleteFilesInDirectory(file2);
            } catch (Throwable e) {
                e.fillInStackTrace();
            }
        } else if (equalAction(STATIC_CLASS, intent.getAction())) {
            String aClass = intent.getStringExtra("class");
            String field = intent.getStringExtra("field");
            String method = intent.getStringExtra("method");
            String[] args = new String[]{};
            try {
                args = intent.getStringArrayExtra("args");
                if (args == null) {
                    args = new String[]{intent.getStringExtra("args")};
                }
            } catch (Throwable e) {
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
        } else if (equalAction(DYN_CLASS, intent.getAction())) {
            String path = intent.getStringExtra("path");
            DynamicClassLoader dynamicClassLoader = new DynamicClassLoader();
            try {
                dynamicClassLoader.callInterface(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 新增调试命令处理逻辑
        else if (equalAction(CLEAR_DIR, intent.getAction())) {
            String dirPath = intent.getStringExtra("dir");
            if (!TextUtils.isEmpty(dirPath)) {
                File dir = new File(dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    boolean cleared = deleteFilesInDirectory(dir);
                    Lg.i(TAG, "Cleared directory %s: %b", dirPath, cleared);
                } else {
                    Lg.i(TAG, "Directory %s does not exist or is not a directory", dirPath);
                }
            }
        } else if (equalAction(PRINT_PROPERTY, intent.getAction())) {
            String key = intent.getStringExtra("key");
            if (!TextUtils.isEmpty(key)) {
                Object property = RefInvoke.invokeStaticMethod("android.os.SystemProperties", "get",
                        new Class[]{String.class}, new Object[]{key});
                Lg.i(TAG, "Property %s: %s", key, property);
            }
        } else if (equalAction(EXECUTE_COMMAND, intent.getAction())) {
            String command = intent.getStringExtra("command");
            if (!TextUtils.isEmpty(command)) {
                try {
                    Process process = Runtime.getRuntime().exec(command);
                    java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";
                    Lg.i(TAG, "Command %s result: %s", command, result);
                } catch (IOException e) {
                    e.printStackTrace();
                    Lg.e(TAG, "Failed to execute command %s: %s", command, e.getMessage());
                }
            }
        }
    }

    /**
     * 递归复制目录
     * @param sourceDir 源目录
     * @param targetDir 目标目录
     * @throws IOException 复制过程中出现 I/O 错误时抛出
     */
    private void copyDirectory(java.nio.file.Path sourceDir, java.nio.file.Path targetDir) throws java.io.IOException {
        java.nio.file.Files.createDirectories(targetDir);
        java.nio.file.Files.walk(sourceDir)
                .forEach(sourcePath -> {
                    try {
                        java.nio.file.Path targetPath = targetDir.resolve(sourceDir.relativize(sourcePath));
                        if (java.nio.file.Files.isDirectory(sourcePath)) {
                            java.nio.file.Files.createDirectories(targetPath);
                        } else {
                            java.nio.file.Files.copy(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (java.io.IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * 删除指定目录下的所有文件
     * @param dir 要清理的目录
     * @return 是否成功清理
     */
    private boolean deleteFilesInDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFilesInDirectory(file);
                    }
                    boolean deleted = file.delete();
                    Lg.i(TAG, "Delete %s:%b", file.getPath(), deleted);
                }
            }
            return true;
        }
        return false;
    }
}
