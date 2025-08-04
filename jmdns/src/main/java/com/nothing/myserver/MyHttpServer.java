package com.nothing.myserver;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
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
        if (context.getApplicationContext() instanceof Application) {
            Application application = (Application) context.getApplicationContext();
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
        if ("/file_sdcard".equals(uri) || "/file_data".equals(uri)) {
            String subDir = getFirstParameterValue(session, "rootPath");
            File baseDir =
                    "/file_sdcard".equals(uri) ? new File(android.os.Environment.getExternalStorageDirectory()
                            .getPath()) : context.getDataDir();

            JSONObject jsonObject = createSubFileListJson(baseDir, subDir);
            return createJsonResponse(Response.Status.OK, jsonObject);
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
                        Response.Status.METHOD_NOT_ALLOWED,
                        "Error taking screenshot: " + e.getMessage()
                );
            }
        }
        return super.serve(session);
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
            Lg.e(TAG,"Current Activity Is Null");
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
            Lg.e(TAG,"take screen shoot fail ,%s",Lg.getStackTraceAsString(e));
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
     * @param status     响应状态
     * @param jsonObject JSON 对象
     * @return 固定长度的响应
     */
    private Response createJsonResponse(Response.Status status, JSONObject jsonObject) {
        return newFixedLengthResponse(status, "application/json", jsonObject.toString());
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

    private static final String TAG = "MyHttpServer";

    @NonNull
    private JSONObject createSubFileListJson(File baseDir, @Nullable String rootPath) {
        File dataDir = new File(baseDir, TextUtils.isEmpty(rootPath) ? "" : rootPath);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (dataDir.exists() && dataDir.isDirectory()) {
            for (File file : Objects.requireNonNull(dataDir.listFiles())) {
                Lg.i(TAG, file.getPath());
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
                    Lg.e(TAG, "JSONException in createSubFileListJson: " + e.getMessage());
                }
                jsonArray.put(object);
            }
        }
        try {
            jsonObject.put("data", jsonArray);
            jsonObject.put("rootPath", dataDir.getPath());
        } catch (JSONException e) {
            // 记录异常信息
            Lg.e(TAG, "JSONException in createSubFileListJson: " + e.getMessage());
        }
        return jsonObject;
    }
}
