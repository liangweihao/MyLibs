package com.nothing.commonutils.utils;

import android.os.Bundle;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GsonUtils {


    @NonNull
    public synchronized static String toJson(Object obj){
        String json = "";
        try {
            json = new Gson().toJson(obj);
        }catch (Throwable e){
            e.fillInStackTrace();
        }
        return json;
    }


    @Nullable
    public synchronized static <T> T toModle(String json,Class<T> tClass){
        try{
            T t = new Gson().fromJson(json, tClass);
            return  t;
        }catch (Throwable t){
            t.fillInStackTrace();
        }
        return null;
    }


    @NonNull
    public synchronized static String bundleToJson(@NonNull Bundle bundle){
        JSONObject json = new JSONObject();
        for (String key : bundle.keySet()) {
            try {
                Object value = bundle.get(key);
                // Convert Bundle value types to JSON compatible types
                if (value instanceof Bundle) {
                    value = bundleToJson((Bundle) value); // Recursively convert nested Bundle
                } else if (value instanceof Boolean || value instanceof Integer ||
                           value instanceof Long || value instanceof Double || value instanceof String) {
                    // Supported types
                } else if (value instanceof int[]) {
                    value = new JSONArray(value); // Convert int[] to JSONArray
                } else if (value instanceof String[]) {
                    value = new JSONArray(value); // Convert String[] to JSONArray
                } else {
                    value = value.toString(); // Convert other types to string
                }
                json.put(key, value);
            } catch (JSONException e) {
                // Handle JSON exception here
                e.printStackTrace();
            }
        }
        return json.toString();
    }
}
