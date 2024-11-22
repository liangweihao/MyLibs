package com.nothing.commonutils.utils;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Size;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;

public class MediaUtils {

    private static final String TAG = "MediaUtils";

    @NonNull
    public static byte[] getByteArrayFromUri(Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            if (inputStream == null) {
                return new byte[0];
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] bytes = byteArrayOutputStream.toByteArray();
            bufferedInputStream.close();
            inputStream.close();
            byteArrayOutputStream.close();
            return bytes;
        } catch (IOException e) {
            e.fillInStackTrace();
            return new byte[0];
        }
    }

    public static Size queryResolution(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    uri,
                    new String[]{MediaStore.MediaColumns.RESOLUTION},
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RESOLUTION);
                String resolution = cursor.getString(columnIndex);
                if (TextUtils.isEmpty(resolution)){
                    return new Size(-1, -1);
                }
                try {
                    String[] split = resolution.split("×");
                    return new Size(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new Size(-1, -1);
    }

    public static long queryDuration(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    uri,
                    new String[]{MediaStore.MediaColumns.DURATION},
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION);
                long duration = cursor.getLong(columnIndex);
                return duration;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    public static String queryData(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    uri,
                    new String[]{MediaStore.MediaColumns.DATA},
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                return cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public static String queryRelativePath(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    uri,
                    new String[]{MediaStore.MediaColumns.RELATIVE_PATH},
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH);
                return cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public static String queryMimeType(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    uri,
                    new String[]{MediaStore.MediaColumns.MIME_TYPE},
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
                return cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public static int queryOrientation(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    uri,
                    new String[]{MediaStore.MediaColumns.ORIENTATION},
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.ORIENTATION);
                return cursor.getInt(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }


    public static JSONArray getJsonArrayFromUri(Context context, Uri uri, String[] projection) {
        JSONArray jsonArray = new JSONArray();
        try {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(uri, projection, null, null, null);
            if (cursor != null) {
                Lg.i(TAG, "getJsonArrayFromUri Cursor Size:" + cursor.getCount());
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 0; i < projection.length; i++) {
                        int columnIndex = cursor.getColumnIndex(projection[i]);
                        if (columnIndex != -1) {
                            switch (cursor.getType(columnIndex)) {
                                case Cursor.FIELD_TYPE_STRING:
                                    jsonObject.put(projection[i], cursor.getString(columnIndex));
                                    break;
                                case Cursor.FIELD_TYPE_INTEGER:
                                    jsonObject.put(projection[i], cursor.getInt(columnIndex));
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    jsonObject.put(projection[i], cursor.getFloat(columnIndex));
                                    break;
                                // 根据需要添加其他数据类型的处理
                            }
                        }
                    }
                    jsonArray.put(jsonObject);
                }
                cursor.close();
            } else {
                Lg.i(TAG, "getJsonArrayFromUri Cursor Null");
            }
        } catch (JSONException | IllegalArgumentException e) {
            e.fillInStackTrace();
        }
        return jsonArray;
    }

    public static String getMediaImageUri() {
        return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL).toString();
    }

    public static String getMediaImageUri(long id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL, id).toString();
        } else {
            return ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), id)
                    .toString();
        }
    }


    public static String getMediaVideoUri() {
        return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL).toString();
    }

    public static String getMediaVideoUri(long id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL, id).toString();
        } else {
            return ContentUris.withAppendedId(MediaStore.Video.Media.getContentUri("external"), id)
                    .toString();
        }
    }

}
