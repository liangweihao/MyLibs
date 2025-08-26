package com.nothing.myserver;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.MimeTypeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import fi.iki.elonen.NanoHTTPD;

public class MyHttpServer extends NanoHTTPD {

    private final Context context;
    private Activity currentActivity;

    public MyHttpServer(Context context, int port) {
        super(port);
        this.context = context;
        registerActivityLife(context);
    }

    private void registerActivityLife(Context context) {
        // 使用模式变量替换显式类型转换
        if (context.getApplicationContext() instanceof Application application) {
            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(
                        @NonNull Activity activity,
                        @Nullable Bundle savedInstanceState
                ) {
                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                    currentActivity = activity;
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(
                        @NonNull Activity activity,
                        @NonNull Bundle outState
                ) {
                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                    if (currentActivity == activity) {
                        currentActivity = null;
                    }
                }
            });
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        // 处理 /file_sdcard 或 /file_data 请求
        if ("/file_sdcard".equals(uri) || "/file_data".equals(uri) || "/file_external_data".equals(
                uri)) {
            String subDir = getFirstParameterValue(session, "rootPath");
            JSONObject jsonObject = createSubFileListJson(uri, subDir);
            return createJsonResponse(jsonObject);
        }

        // 处理 /download_file 请求
        if ("/download_file".equals(uri)) {
            String filePath = getFirstParameterValue(session, "filePath");
            if (filePath == null) {
                return createTextResponse(
                        Response.Status.BAD_REQUEST,
                        "Missing 'filePath' parameter"
                );
            }

            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                return createTextResponse(Response.Status.NOT_FOUND, "File not found");
            }

            try {
                return createFileResponse(file);
            } catch (java.io.FileNotFoundException e) {
                return createTextResponse(Response.Status.NOT_FOUND, "Error opening file");
            }
        }

        // 处理 /screenshot 指令
        if ("/screenshot".equals(uri)) {
            try {
                // 调用截图方法
                File screenshotFile = takeScreenshot();
                if (screenshotFile != null && screenshotFile.exists()) {
                    return createFileResponseWithAutoDelete(screenshotFile);
                } else {
                    return createTextResponse(
                            Response.Status.METHOD_NOT_ALLOWED,
                            "Failed to take screenshot"
                    );
                }
            } catch (Exception e) {
                return createTextResponse(
                        Response.Status.NOT_FOUND,
                        "Error taking screenshot: " + e.getMessage()
                );
            }
        }

        // 处理 /device_info 请求
        if ("/device_info".equals(uri)) {
            try {
                JSONObject deviceInfo = getDeviceInfo();
                return createJsonResponse(deviceInfo);
            } catch (JSONException e) {
                return createTextResponse(
                        Response.Status.NOT_FOUND,
                        "Error getting device info: " + e.getMessage()
                );
            }
        }

        // 处理 /installed_apps 请求
        if ("/installed_apps".equals(uri)) {
            try {
                JSONArray installedApps = getInstalledAppsInfo();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("installed_apps", installedApps);
                return createJsonResponse(jsonObject);
            } catch (JSONException e) {
                return createTextResponse(
                        Response.Status.NOT_FOUND,
                        "Error getting installed apps info: " + e.getMessage()
                );
            }
        }

        // 新增：处理 /download_apk 请求
        if ("/download_apk".equals(uri)) {
            String apkPath = getFirstParameterValue(session, "apkPath");
            if (apkPath == null) {
                return createTextResponse(
                        Response.Status.BAD_REQUEST,
                        "Missing 'apkPath' parameter"
                );
            }

            File apkFile = new File(apkPath);
            if (!apkFile.exists() || !apkFile.isFile()) {
                return createTextResponse(Response.Status.NOT_FOUND, "APK file not found");
            }

            try {
                return createApkResponse(apkFile);
            } catch (FileNotFoundException e) {
                return createTextResponse(Response.Status.NOT_FOUND, "Error opening APK file");
            }
        }

        return super.serve(session);
    }


    /**
     * 创建 APK 下载响应
     *
     * @param file 要下载的 APK 文件
     * @return 固定长度的响应
     * @throws FileNotFoundException 文件未找到异常
     */
    private Response createApkResponse(File file) throws FileNotFoundException {
        return newFixedLengthResponse(
                Response.Status.OK,
                "application/vnd.android.package-archive",
                new FileInputStream(file),
                file.length()
        );
    }


    /**
     * 获取设备信息
     *
     * @return 包含设备信息的 JSONObject
     * @throws JSONException JSON 异常
     */
    @NonNull
    private JSONObject getDeviceInfo() throws JSONException {
        JSONObject deviceInfo = new JSONObject();
        // 系统信息
        deviceInfo.put("brand", Build.BRAND);
        deviceInfo.put("model", Build.MODEL);
        deviceInfo.put("device", Build.DEVICE);
        deviceInfo.put("product", Build.PRODUCT);
        deviceInfo.put("manufacturer", Build.MANUFACTURER);
        deviceInfo.put("android_version", Build.VERSION.RELEASE);
        deviceInfo.put("sdk_version", Build.VERSION.SDK_INT);

        try {
            String serial = Build.getSerial();
            deviceInfo.put("serial_number", serial);
        } catch (Exception e) {
            // 增加 SN 获取字段
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            deviceInfo.put("serial_number", androidId);
         }

        // 存储信息
        deviceInfo.put("total_internal_storage", DeviceUtils.getTotalInternalStorage());
        deviceInfo.put("available_internal_storage", DeviceUtils.getAvailableInternalStorage());
        if (DeviceUtils.isExternalStorageAvailable()) {
            deviceInfo.put("total_external_storage", DeviceUtils.getTotalExternalStorage());
            deviceInfo.put("available_external_storage", DeviceUtils.getAvailableExternalStorage());
        } else {
            deviceInfo.put("external_storage_available", false);
        }
        // 内存信息
        deviceInfo.put("total_ram", DeviceUtils.getTotalRam());
        deviceInfo.put("available_ram", DeviceUtils.getAvailableRam());

        return deviceInfo;
    }

    /**
     * 获取已安装应用的信息
     * @return 包含应用信息的 JSONArray
     * @throws JSONException JSON 异常
     */
    private JSONArray getInstalledAppsInfo() throws JSONException {
        JSONArray appList = new JSONArray();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {
            JSONObject appInfo = new JSONObject();
            appInfo.put("package_name", packageInfo.packageName);
            appInfo.put("app_name", packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
            appInfo.put("version_name", packageInfo.versionName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appInfo.put("version_code", packageInfo.getLongVersionCode());
            } else {
                appInfo.put("version_code", packageInfo.versionCode);
            }
            appInfo.put("is_system_app", (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            // 获取应用 APK 文件路径
            String apkPath = packageInfo.applicationInfo.sourceDir;
            appInfo.put("app_path", apkPath);

            // 判断是否能够下载 APK
            boolean canDownloadApk = canDownloadApk(apkPath);
            appInfo.put("can_download_apk", canDownloadApk);

            appList.put(appInfo);
        }
        return appList;
    }

    /**
     * 判断是否能够下载 APK 文件
     * @param apkPath APK 文件的路径
     * @return 能够下载返回 true，否则返回 false
     */
    private boolean canDownloadApk(String apkPath) {
        File apkFile = new File(apkPath);
        // 检查文件是否存在且为文件，并且应用有读取权限
        return apkFile.exists() && apkFile.isFile() && apkFile.canRead();
    }
    /**
     * 创建文件下载响应，并在响应结束后删除文件
     *
     * @param file 要下载的文件
     * @return 固定长度的响应
     * @throws java.io.FileNotFoundException 文件未找到异常
     */
    private Response createFileResponseWithAutoDelete(File file) throws FileNotFoundException {
        return new FileResponseWithAutoDelete(
                Response.Status.OK,
                MimeTypeUtils.getMimeTypeByExtension(MimeTypeUtils.getFileExtension(file)),
                new FileInputStream(file),
                file.length(),
                file
        );
    }

    /**
     * 实现截图逻辑，需要根据实际情况实现
     *
     * @return 截图文件，如果失败返回 null
     */
    @Nullable
    private File takeScreenshot() {
        if (currentActivity == null) {
            Lg.e("MyHttpServer", "Current Activity Is Null");
            return null;
        }

        // 获取当前 Activity 的 decorView
        android.view.View decorView = currentActivity.getWindow().getDecorView();
        Bitmap bitmap = Bitmap.createBitmap(
                decorView.getWidth(),
                decorView.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        decorView.draw(canvas);

        // 创建截图文件
        File screenshotFile = new File(
                context.getCacheDir(),
                "screenshot_" + System.currentTimeMillis() + ".png"
        );

        try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
            // 将 Bitmap 保存为 PNG 文件
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            Lg.e("MyHttpServer", "take screen shoot fail ,%s", Lg.getStackTraceAsString(e));
            return null;
        } finally {
            // 回收 Bitmap 资源
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        return screenshotFile.exists() ? screenshotFile : null;
    }

    /**
     * 从请求参数中获取第一个参数值
     *
     * @param session       请求会话
     * @param parameterName 参数名
     * @return 第一个参数值，如果不存在则返回 null
     */
    private String getFirstParameterValue(IHTTPSession session, String parameterName) {
        List<String> parameterList = session.getParameters().get(parameterName);
        return parameterList != null && !parameterList.isEmpty() ? parameterList.get(0) : null;
    }

    /**
     * 创建 JSON 响应
     *
     * @param jsonObject JSON 对象
     * @return 固定长度的响应
     */
    private Response createJsonResponse(JSONObject jsonObject) {
        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonObject.toString());
    }

    /**
     * 创建文本响应
     *
     * @param status  响应状态
     * @param message 响应消息
     * @return 固定长度的响应
     */
    private Response createTextResponse(Response.Status status, String message) {
        return newFixedLengthResponse(status, "text/plain", message);
    }

    /**
     * 创建文件下载响应
     *
     * @param file 要下载的文件
     * @return 固定长度的响应
     * @throws java.io.FileNotFoundException 文件未找到异常
     */
    private Response createFileResponse(File file) throws java.io.FileNotFoundException {
        return newFixedLengthResponse(
                Response.Status.OK,
                MimeTypeUtils.getMimeTypeByExtension(MimeTypeUtils.getFileExtension(file)),
                new java.io.FileInputStream(file),
                file.length()
        );
    }

    @NonNull
    private JSONObject createSubFileListJson(String uri, @Nullable String rootPath) {
        File baseDir = new File(android.os.Environment.getExternalStorageDirectory().getPath());

        if ("/file_data".equals(uri)) {
            baseDir = context.getDataDir();
        } else if ("/file_external_data".equals(uri)) {
            baseDir = Objects.requireNonNull(context.getExternalFilesDir(null)).getParentFile();
        }
        File dataDir = new File(baseDir, TextUtils.isEmpty(rootPath) ? "" : rootPath);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (dataDir.exists() && dataDir.isDirectory()) {
            for (File file : Objects.requireNonNull(dataDir.listFiles())) {
                Lg.i("MyHttpServer", file.getPath());
                JSONObject object = new JSONObject();
                try {
                    object.put("path", file.getPath());
                    object.put("length", file.length());
                    object.put("modifyTime", file.lastModified());
                    String mimeType = MimeTypeUtils.getFileExtension(file);
                    if (mimeType.isEmpty()) {
                        object.put("mimeType", "");
                    } else {
                        object.put("mimeType", MimeTypeUtils.getMimeTypeByExtension(mimeType));
                    }
                } catch (JSONException e) {
                    // 记录异常信息
                    Lg.e(
                            "MyHttpServer",
                            "JSONException in createSubFileListJson: " + e.getMessage()
                    );
                }
                jsonArray.put(object);
            }
        }
        try {
            jsonObject.put("data", jsonArray);
            jsonObject.put("rootPath", dataDir.getPath());
        } catch (JSONException e) {
            // 记录异常信息
            Lg.e("MyHttpServer", "JSONException in createSubFileListJson: " + e.getMessage());
        }
        return jsonObject;
    }
}
