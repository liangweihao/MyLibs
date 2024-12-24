package com.nothing.commonutils.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class BugReporterZip {

    private static final String TAG = "BugReporterZip";
    public static void zipLogFiles(Context context,File[] targetDir) {
        // 检查外部存储是否可用
        if (!isExternalStorageWritable()) {
            Lg.e(TAG,"EXTERNAL STORAGE NOT MEDIA_MOUNTED");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        // 创建临时的zip文件
        File zipFile = new File(context.getExternalFilesDir(null).getParentFile(), timestamp+"_log_files.zip");
        try  {
            // 遍历log目录下的文件并添加到zip文件
            for (File file : targetDir) {
                addFilesToZip(file, zipFile,"");
            }

            // 将zip文件移动到Download目录
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            File newZipFile = new File(downloadDir, zipFile.getName());
            if (newZipFile.exists()) {
                newZipFile.delete();
            }
            boolean success = zipFile.renameTo(newZipFile);
            if (!success) {
                // 处理移动失败的情况
            }else {
                Lg.i(TAG,"Save BugReport File:%s",newZipFile.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private static void addFilesToZip(File dir, File saveFile ,String basePath) throws IOException {
        net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(saveFile);
        File[] files = dir.listFiles();
        if (files!= null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addFilesToZip(file,saveFile,basePath+file.getName() + "/");
                } else {
                    // 使用Zip4j库添加文件到zip
                    net.lingala.zip4j.model.ZipParameters zipParameters = new net.lingala.zip4j.model.ZipParameters();
                    zipParameters.setFileNameInZip(basePath + file.getName());
                    zipFile.addFile(file, zipParameters);
                }
            }
        }
    }

}
