package com.nothing.myserver;

import android.content.Context;
import android.text.TextUtils;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.MimeTypeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import fi.iki.elonen.NanoHTTPD;


public class MyHttpServer extends NanoHTTPD {

    private final Context context;

    public MyHttpServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getUri().equals("/file_sdcard") || session.getUri().equals("/file_data")) {
            List<String> rootPath = session.getParameters().get("rootPath");
            String subDir = "";
            if (rootPath != null && !rootPath.isEmpty()) {
                subDir = rootPath.get(0);
            }

            JSONObject jsonObject = createSubFileListJson(
                    session.getUri()
                            .equals("/file_sdcard") ? new File("/sdcard"): context.getDataDir(),
                    subDir
            );
            // 返回包含文件列表的响应
            return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    jsonObject.toString()
            );
        } else if (session.getUri().equals("/download_file")) {
            // 获取下载文件的路径参数
            List<String> filePathList = session.getParameters().get("filePath");
            if (filePathList == null || filePathList.isEmpty()) {
                // 若未提供文件路径，返回错误响应
                return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "text/plain",
                        "Missing 'filePath' parameter"
                );
            }
            String filePath = filePathList.get(0);
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                // 若文件不存在或不是文件，返回错误响应
                return newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        "text/plain",
                        "File not found"
                );
            }

            try {
                // 返回文件下载响应
                return newFixedLengthResponse(
                        Response.Status.OK,
                        MimeTypeUtils.getMimeTypeByExtension(MimeTypeUtils.getFileExtension(file)),
                        new java.io.FileInputStream(file),
                        file.length()
                );
            } catch (java.io.FileNotFoundException e) {
                // 处理文件未找到异常
                return newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        "text/plain",
                        "Error opening file"
                );
            }
        }
        return super.serve(session);
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
                }
                jsonArray.put(object);
            }
        }
        try {
            jsonObject.put("data", jsonArray);
            jsonObject.put("rootPath", dataDir.getPath());
        } catch (JSONException e) {
        }
        return jsonObject;
    }
}
